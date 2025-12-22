package edu.kh.project.common.scheduling;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.kh.project.board.model.service.BoardService;
import lombok.extern.slf4j.Slf4j;

@Component //IOC 관련 : Bean 등록
@Slf4j
@PropertySource("classpath:config.properties")

public class ImageDeleteScheduling {

	@Autowired
    private BoardService service;

    @Value("${my.profile.folder-path}")
	private String profileFolderPath;

	@Value("${my.board.folder-path}")
	private String boardFolderPath;
	
	@Scheduled(cron = "0 30 0 * * *")
    public void scheduling() {
		log.info("스케쥴러 동작!");
        //DB, 서버 폴더의 파일 목록 비교 후
        //DB에 없는 서버 이미지 파일 삭제 동작

        //1. 서버 폴더의 파일 목록 조회하기
        File boardFolder = new File(boardFolderPath);
        File memberFolder = new File(profileFolderPath);

        //참조하는 폴더에 존재하는 파일 목록 얻어오기
        File[] boardArr = boardFolder.listFiles();
        File[] memberArr = memberFolder.listFiles();

        // null 체크 및 빈 배열 처리
        if(boardArr == null) boardArr = new File[0];
        if(memberArr == null) memberArr = new File[0];

        //두 배열을 하나로 합침
        //imageArr 라는 빈 배열을 boardArr과 memberArr 길이 만큼의 크기로 만들기
        File[] imageArr = new File[boardArr.length + memberArr.length]; // -> 길이 변경 불가

        //배열 내용 복사(깊은 복사)
        //System.arraycopy(복사할 배열, 몇번 인덱스부터 복사할지,
        //새로운 배열, 새로운 배열의 몇번부터 넣을지 인덱스, 복사를 어디까지 할건지)
        if(memberArr.length > 0) {
            System.arraycopy(memberArr, 0, imageArr, 0, memberArr.length);
        }
        if(boardArr.length > 0) {
            System.arraycopy(boardArr, 0, imageArr, memberArr.length, boardArr.length);
        }

        //배열 -> List 변환(다루기 쉽도록)
        List<File> serverImageList = Arrays.asList(imageArr);

        //2. DB 이미지 파일 이름만 모두 조화
        List<String> dbImageList = service.selectImageList();

        //3. 서버, DB 이미지 파일명을 비교하여
        //서버에만 있고, DB에 없는 파일을 서버에서 삭제 
        if(!serverImageList.isEmpty() && !dbImageList.isEmpty()) {
            for(File serverImage : serverImageList) {
                //File.getName() : 서버 파일 이름
                //List.indexOf(객체) :
                //List에 전달한 객체가 존재하면 존재하는 index 반환
                //존재하지 않으면 -1 반환하는 메서드
                if(dbImageList.indexOf(serverImage.getName()) == -1) {
                    serverImage.delete();
                    log.info(serverImage.getName() + "삭제");
                }
            }
        }
    }
}



/*
* @Scheduled
*
* * Spring에서 제공하는 스케줄러 : 시간에 따른 특정 작업(Job)의 순서를 지정하는 방법.
*
* 설정 방법
* 1) XXXAPPlication.java 파일에 @EnableScheduling 어노테이션 추가
* 2) 스케쥴링 동작을 위한 클래스 작성
*
*
* @Scheduled 속성
*  - fixedDelay : 이전 작업이 끝난 시점으로 부터 고정된 시간(ms)을 설정.
*    @Scheduled(fixedDelay = 10000) // 이전 작업이 끝난 후 10초 뒤에 실행
*  
*  - fixedRate : 이전 작업이 수행되기 시작한 시점으로 부터 고정된 시간(ms)을 설정.
*    @Scheduled(fixedRate  = 10000) // 이전 작업이 시작된 후 10초 뒤에 실행
* 
*  
*  
* * cron 속성 : UNIX계열 잡 스케쥴러 표현식으로 작성 - cron="초 분 시 일 월 요일 [년도]" - 요일 : 1(SUN) ~ 7(SAT)
* ex) 2019년 9월 16일 월요일 10시 30분 20초 cron="20 30 10 16 9 2 " // 연도 생략 가능
*
*
*  @Scheduled(cron = "* * * * * 30")
*  @Scheduled(cron = "0 0 12 * * *")
*
*
* - 특수문자
* * : 모든 수.
* - : 두 수 사이의 값. ex) 10-15 -> 10이상 15이하
* , : 특정 값 지정. ex) 3,4,7 -> 3,4,7 지정
* / : 값의 증가. ex) 0/5 -> 0부터 시작하여 5마다
* ? : 특별한 값이 없음. (월, 요일만 해당)
* L : 마지막. (월, 요일만 해당)
* @Scheduled(cron="0 * * * * *") // 모든 0초 마다 -> 매 분마다 실행
*
*/

// 예약은 서비스가 분리되어 있어서 API Gateway 없이 테스트 불가 (CORS 터짐)
const API_GATEWAY_HOST = "http://localhost:9000"

/* 접속중인 이용자 정보 logging */
console.warn("memberRole:", memberRole);
console.warn("memberCode:", memberCode);
console.warn("requestUuid:", requestUuid);


/*
    검색 컨트롤 함수
        1. search: 검색 API request - 검색 버튼과 연결되어 있음.
        2. keypress event listener: 엔터 입력 시 검색 API request
*/
function search() {
    const keyword = document.getElementById('searchbar-input').value;
    location.href = `${API_GATEWAY_HOST}/search?q=${keyword}`;
}


/* 로그인 함수 */
function signIn() {
    // 로그인 오버레이
    const overlay = document.getElementById('signin-overlay');

    // 로그인 입력 정보
    const memberId = document.getElementById('signin_id').value;
    const memberPw = document.getElementById('signin_pw').value;

    axios.post(`${API_GATEWAY_HOST}/v1/auth/sign-in`, {
        member_id: memberId,
        member_pw: memberPw
    }).then(function (response) {
        console.log(response);
        overlay.style.visibility = "hidden";
        location.reload();
    }).catch(function (error) {
        console.log(error);
        if (error.status === 404) alert("아이디 또는 비밀번호가 틀렸습니다.");
        else alert("서버와의 통신에 실패했습니다.");
    });
}

window.onload = () => { // document 렌더링 후 enter key 이벤트 연결
    // 하나의 onload 콜백 함수에 다 몰아서 작성해야 함.
    document.getElementById('searchbar-input').addEventListener('keypress', event => {
        if (event.key === 'Enter') {
            search();
        }
    });
    document.getElementById('signin_id').addEventListener('keypress', event => {
        if (event.key === 'Enter') {
            signIn();
        }
    });
    document.getElementById('signin_pw').addEventListener('keypress', event => {
        if (event.key === 'Enter') {
            signIn();
        }
    });
}

function signOut() {
    const ok = confirm("로그아웃 하시겠습니까?");
    if (ok) {
        axios.delete(`${API_GATEWAY_HOST}/v1/auth/sign-out`
        ).then(function (response) {
            console.log(response);
            location.href = '/';
        }).catch(function (error) {
            console.log(error);
            alert("서버와의 통신에 실패했습니다.");
        });
    }
}

/* 로그아웃 함수 */
function signUp() {
    location.href = `${API_GATEWAY_HOST}/auth/sign-up`;
}

/*
    로그인 오버레이 컨트롤
        1. showOverlay: 오버레이 보여주는 함수
        2. mouseup event listener: 오버레이 숨기는 함수
*/
function showOverlay() {
    // 로그인 오버레이
    // const overlay = document.getElementById('signin-overlay');
    const box = document.getElementById('signin-box');
    box.style.opacity = 0;
    $("#signin-overlay")
        .css("display", "flex")
        .hide()
        .fadeIn('fast');

    setTimeout(() => {
        box.style.opacity = 100;
        $("#signin-box")
            .css("display", "flex")
            .hide()
            .fadeIn(450);
    }, 120);
}

window.addEventListener('mouseup',function(event){
    // 로그인 오버레이
    const overlay = document.getElementById('signin-overlay');

    // signin-box 외부 클릭 시 overlay 숨기기
    if(!(event.target.closest("#signin-box"))){
        $("#signin-overlay").fadeOut(400);
    }
});

/* 로그인 창 입력값 유효성 검사 함수 */
function checkLoginAvailable() {
    const idValue = document.getElementById('signin_id').value;
    const pwValue = document.getElementById('signin_pw').value;
    const button = document.getElementById('signin-button');

    if (idValue.length > 0 && pwValue.length > 0) {
        button.classList.add('active');
        button.onclick = signIn;
    }
    else {
        button.classList.remove('active');
        button.onclick = null;
    }
}

/* 좌측 서비스 로고 버튼 클릭시 작동하는 함수 */
function gotoRoot() {
    location.href = "/";
}

/* '나의 예약' 버튼 클릭시 작동하는 함수 */
function gotoMyReservationPage() {
    if (memberRole !== 'MEMBER') {
        alert("미리보기 모드입니다.");
        return;
    }
    location.href = `${API_GATEWAY_HOST}/member/reserve`;
}


/*
    SSE 알림
*/
if (memberRole === "MEMBER" && memberCode != null) { // 이용자 로그인 상태에서만 SSE 수신
    const eventSource = new EventSource(`${API_GATEWAY_HOST}/noti/subscribe`);

    // SSE 최초 연결시
    eventSource.onopen = function () {
        console.log('SSE 연결 성공');
        console.log('memberCode:', memberCode);
    };

    // SSE 이벤트 발생시마다 --> custom type 용
    // 이용자는 RESERVE_RESULT type에 대한 이벤트만 수신함.
    eventSource.addEventListener("RESERVE_RESULT", (event) => {
        // const message = event.data;
        console.log('Received message:', event.data); // logging

        // 현재의 URL에 따른 동적 뷰 처리
        if (window.location.href === `${API_GATEWAY_HOST}/member/reserve`) { // 1. 나의 예약 페이지면
            console.log('이벤트 수신 -> 나의 예약 테이블 업데이트');
            updateView();
        }

        // 토스트 뷰 처리
        const message = event.data;
        const dateTime = moment(event.start).format('YYYY-MM-DD HH:mm:ss'); // moment는 cdn으로 로드됨

        showAlarmToast(message, dateTime);
    });

    // 토스트 뷰 컨트롤
    function showAlarmToast(message, dateTime) {
        console.log('show toast'); // logging
        // parent div (toast box)
        const notiToastBoxDiv = document.getElementById('noti-toast-box');

        // old div
        const notiToastDataDiv = document.getElementById('noti-toast-data');
        const notiToastDatetimeDiv = document.getElementById('noti-toast-datetime');

        // new div
        const newDataDiv = document.createElement("div");
        newDataDiv.classList.add('noti-toast-data');
        newDataDiv.id = 'noti-toast-data';
        newDataDiv.appendChild(document.createTextNode(message));

        const newDatetimeDiv = document.createElement("div");
        newDatetimeDiv.classList.add('noti-toast-datetime');
        newDatetimeDiv.id = 'noti-toast-datetime';
        newDatetimeDiv.appendChild(document.createTextNode(dateTime));

        // div 교체
        notiToastDataDiv.replaceWith(newDataDiv);
        notiToastDatetimeDiv.replaceWith(newDatetimeDiv);

        // 토스트 박스 보여주기
        notiToastBoxDiv.classList.add("active");

        // 5초 후 토스트 박스 숨기기
        setTimeout(() =>{
            console.log('hide toast'); // logging
            notiToastBoxDiv.classList.remove("active");
        }, 5000)
    }
}
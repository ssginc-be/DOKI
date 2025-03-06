function signIn() {
    // 로그인 오버레이
    const overlay = document.getElementById('signin-overlay');

    // 로그인 입력 정보
    const memberId = document.getElementById('signin_id').value;
    const memberPw = document.getElementById('signin_pw').value;

    axios.post("http://localhost:9093/v2/auth/sign-in", {
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

function signOut() {
    const ok = confirm("로그아웃 하시겠습니까?");
    if (ok) {
        axios.delete("http://localhost:9093/v2/auth/sign-out"
        ).then(function (response) {
            console.log(response);
            location.reload();
        }).catch(function (error) {
            console.log(error);
            alert("서버와의 통신에 실패했습니다.");
        });
    }
}

function showOverlay() {
    // 로그인 오버레이
    const overlay = document.getElementById('signin-overlay');

    overlay.style.visibility = "visible";
}

window.addEventListener('mouseup',function(event){
    // 로그인 오버레이
    const overlay = document.getElementById('signin-overlay');

    // signin-box 외부 클릭 시 overlay 숨기기
    if(!(event.target.closest("#signin-box"))){
        overlay.style.visibility = "hidden";
    }
});


function gotoRoot() {
    location.href = "/";
}

function gotoMyReservationPage() {
    location.href = "http://localhost:9093/member/reserve";
}


/*
    SSE 알림
*/
if (memberCode != null) { // 로그인 상태에서만 SSE 수신
    const eventSource = new EventSource('http://localhost:9093/noti/subscribe');

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
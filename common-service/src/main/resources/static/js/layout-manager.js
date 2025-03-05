function signOut() {
    const ok = confirm("로그아웃 하시겠습니까?");
    if (ok) {
        axios.delete("http://localhost:9093/v2/auth/sign-out"
        ).then(function (response) {
            console.log(response);
            location.replace("http://localhost:9093"); // 팝업스토어 목록 조회 페이지로 이동
        }).catch(function (error) {
            console.log(error);
            alert("서버와의 통신에 실패했습니다.");
        });
    }
}

function gotoRoot() {
    location.href = "/";
}

function gotoStoreReservationPage() {
    location.href = "http://localhost:9093/store/reserve";
}


/*
    SSE 알림
*/
let notiCount = 0; // 최초 페이지 로딩 시 초기 알림 개수
const eventSource = new EventSource('http://localhost:9093/noti/subscribe');

// SSE 최초 연결시
eventSource.onopen = function() {
    console.log('SSE 연결 성공');
};

// SSE 이벤트 발생시마다 --> 'message' 타입에만 동작하므로, 프로젝트에서 사용하지 않음
// eventSource.onmessage = (event) => {
//     const message = event.data;
//     console.log('Received message:', message); // logging
//
//     // 알림 개수 증가
//     notiCount += 1;
//
//     // 뷰 업데이트
//     const alertBoxDiv = document.getElementById("navbar-alert-box"); // div
//     const bellBoxImg = document.getElementById("navbar-bell-icon"); // img
//
//     alertBoxDiv.innerHTML = `확인하지 않은 알림이 ${notiCount}건 있습니다.`;
//     bellBoxImg.src = "/icon/layout/bell_on_dark.svg";
// };

// SSE 이벤트 발생시마다 --> custom type 용
// 운영자는 RESERVE_REQUEST type에 대한 이벤트만 수신함.
eventSource.addEventListener("RESERVE_REQUEST", (event) => {
    const message = event.data;
    console.log('Received message:', message); // logging

    // 알림 개수 증가
    notiCount += 1;

    // 뷰 업데이트
    const alertBoxDiv = document.getElementById("navbar-alert-box"); // div
    const bellBoxImg = document.getElementById("navbar-bell-icon"); // img

    alertBoxDiv.innerHTML = `확인하지 않은 알림이 ${notiCount}건 있습니다.`;
    bellBoxImg.src = "/icon/layout/bell_on_dark.svg";
})
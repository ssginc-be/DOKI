/* 페이지 로딩시마다 알림 내역 가져오는 함수 */
axios.get("http://localhost:9093/noti/all"
).then(function (response) {
    console.log(response);
    notiList = response.data
    notiCount = response.data.length;
    if (notiCount > 0) {
        updateIndicator();
        notiList.forEach(noti => {
            addSingleElementToAlarmList(noti.data, noti.dateTime);
        });
    }
}).catch(function (error) {
    console.log(error);
    alert("알림을 가져오는데 실패했습니다.");
});


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

/* 알림 버튼 클릭시 동작하는 함수 */
function toggleAlertListBox() {
    const div = document.getElementById('navbar-alert-list-box');

    if (div.style.visibility === 'visible')  {
        div.style.visibility = 'hidden';
    } else {
        div.style.visibility = 'visible';

        // 모든 알림 읽음 처리 (삭제)
        axios.delete("http://localhost:9093/noti/all"
        ).then(function (response) {
            console.log(response);
        }).catch(function (error) {
            console.log(error);
            alert("서버와의 통신에 실패했습니다.");
        });
    }
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
    // const message = event.data;
    console.log('Received message:', event.data); // logging

    // 알림 개수 증가
    notiCount += 1;

    // 뷰 업데이트
    // 1. 인디케이터 뷰 업데이트
    const message = event.data;
    const dateTime = moment(event.start).format('YYYY-MM-DD HH:mm:ss'); // moment는 cdn으로 로드됨
    updateIndicator();

    // 2. 알림 리스트 뷰에 element 추가
    addSingleElementToAlarmList(message, dateTime);

});

function updateIndicator() {
    const alertBoxDiv = document.getElementById("navbar-alert-box"); // div
    const bellBoxImg = document.getElementById("navbar-bell-icon"); // img

    alertBoxDiv.innerHTML = `확인하지 않은 알림이 ${notiCount}건 있습니다.`;
    bellBoxImg.src = "/icon/layout/bell_on_dark.svg";
}

function addSingleElementToAlarmList(message, dateTime) {
    const alertListBoxDiv = document.getElementById("navbar-alert-list-box");

    const alertElementDiv = document.createElement("div")
    alertElementDiv.classList.add('navbar-alert-element');

    const alertElementDataDiv = document.createElement("div")
    alertElementDataDiv.classList.add('navbar-alert-element-data');
    alertElementDataDiv.appendChild(document.createTextNode(message));

    const alertElementTimeDiv = document.createElement("div")
    alertElementTimeDiv.classList.add('navbar-alert-element-time');
    alertElementTimeDiv.appendChild(document.createTextNode(dateTime));

    alertElementDiv.appendChild(alertElementDataDiv);
    alertElementDiv.appendChild(alertElementTimeDiv);

    alertListBoxDiv.appendChild(alertElementDiv);
}
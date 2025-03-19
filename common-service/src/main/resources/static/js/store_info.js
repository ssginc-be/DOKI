// layout-member.js에 API_GATEWAY_HOST 이미 선언되어 있음.

// 접속한 사용자의 memberCode 확인
console.log('memberCode:', memberCode);


function handleImageBoxClicked(event) {
    // img src 가져오기
    const imageLink = event.firstChild.src;
    console.log("clicked image link:", imageLink);
    window.open(imageLink, '_blank');
}

/* '목록으로' 버튼 클릭시 작동하는 함수 */
function goBack() {
    if (memberRole === 'MANAGER') {
        alert("미리보기 모드입니다.");
        return;
    }
    history.back();
}

/* [이용자] '예약하기' 버튼 클릭시 작동하는 함수 */
function gotoReservationPage(storeId) {
    location.href = API_GATEWAY_HOST + "/reserve?id=" + storeId;
}

/* 비로그인 상태에서 예약하기 버튼 클릭시 작동하는 함수 */
function askSignIn() {
    const ok = confirm("로그인이 필요한 서비스입니다.\n로그인하시겠습니까?");
    if (ok) showOverlay(); // layout-member.js의 로그인 오버레이 함수 호출
}

/* [운영자] 미리보기 모드에서 예약하기 버튼 클릭시 작동하는 함수 */
function alertPreview() {
    alert("미리보기 모드입니다.");
}
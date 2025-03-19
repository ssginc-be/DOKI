// layout-manager.js에 API_GATEWAY_HOST 이미 선언되어 있음.

// 접속한 MANAGER의 memberCode 확인
console.log('memberCode:', memberCode);


function handleImageBoxClicked(event) {
    // img src 가져오기
    const imageLink = event.firstChild.src;
    console.log("clicked image link:", imageLink);
    window.open(imageLink, '_blank');
}

function handlePreviewButtonClicked(sid) {
    // memberCode는 전역에서 접근 가능
    window.open(API_GATEWAY_HOST + "/store?id=" + sid, '_blank');
}
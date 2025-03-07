function gotoRoot() {
    location.href = "/";
}

/* sidebar 메뉴 클릭시 페이지 이동하는 용도 */
function gotoPage(idx) {
    switch (idx) {
        case 0: location.href = "http://localhost:9093"; break;
        case 1: location.href = "http://localhost:9093"; break;
        case 2: location.href = "http://localhost:9093"; break;
        case 3: location.href = "http://localhost:9093"; break;
        case 4: location.href = "http://localhost:9093"; break;
        default: alert("잘못된 접근입니다."); break;
    }
}

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
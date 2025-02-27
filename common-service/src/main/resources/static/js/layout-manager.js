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
        location.replace("http://localhost:9093");
    }).catch(function (error) {
        console.log(error);
        alert("서버와의 통신에 실패했습니다.");
    });
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
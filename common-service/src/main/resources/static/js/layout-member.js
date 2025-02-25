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
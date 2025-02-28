function reserve() {
    // getElementById로 예약 데이터 가져와서 body 구성
    //

    const ok = confirm("예약하시겠습니까?");
    if (ok) {
        // axios 호출
        axios.post("http://localhost:9091/v2/reserve", {
            member_id: memberId,
            member_pw: memberPw
        }).then(function (response) {
            console.log(response);
            alert("예약 신청이 완료되었습니다. 신청 결과는 '나의 예약'에서 확인 가능합니다.");
            location.reload();
        }).catch(function (error) {
            console.log(error);
            alert("서버와의 통신에 실패했습니다.");
        });
    }
}

function goBack() {
    const ok = confirm("작성을 취소하고 뒤로 가시겠습니까?");
    if (ok) history.back();
}
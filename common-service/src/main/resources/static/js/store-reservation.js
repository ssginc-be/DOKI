function reserve() {
    // getElementById로 예약 데이터 가져와서 body 구성하고 axios 호출

    const ok = confirm("예약하시겠습니까?");
    if (ok) alert("예약 등록 완료");
}

function goBack() {
    const ok = confirm("작성을 취소하고 뒤로 가시겠습니까?");
    if (ok) history.back();
}
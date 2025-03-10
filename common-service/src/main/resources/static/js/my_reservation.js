function cancelReservation(rid) {  // rid: reservation id
    const ok = confirm("예약을 취소하시겠습니까?");
    if (ok) {
        getRequest("http://localhost:9093/v1/member/reserve/cancel?id=" + rid);
    }
}

function getRequest(endpoint) {
    axios.get(endpoint
    ).then(function (response) {
        console.log(response);
    }).catch(function (error) {
        console.log(error);
        alert("서버와의 통신에 실패했습니다.");
    });
}
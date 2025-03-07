function confirmReservation(rid) { // rid: reservation id
    const ok = confirm("예약을 승인하시겠습니까?");
    if (ok) {
        putRequest("http://localhost:9093/v1/store/reserve/confirm?id=" + rid);
    }
}

function refuseReservation(rid) {  // rid: reservation id
    const ok = confirm("예약을 거절하시겠습니까?");
    if (ok) {
        putRequest("http://localhost:9093/v1/store/reserve/refuse?id=" + rid);
    }
}

function cancelReservation(rid) {  // rid: reservation id
    const ok = confirm("예약을 취소하시겠습니까?");
    if (ok) {
        putRequest("http://localhost:9093/v1/store/reserve/cancel?id=" + rid);
    }
}

function putRequest(endpoint) {
    axios.put(endpoint
    ).then(function (response) {
        console.log(response);
    }).catch(function (error) {
        console.log(error);
        alert("서버와의 통신에 실패했습니다.");
    });
}
/*  */
const MIN_ROW = 8; // 데이터가 8개보다 적어도, row가 최소 8개 렌더링되도록 함
async function updateView() {
    console.log("update view");
    // 1. 데이터 가져오기
    const dtoList = await getStoreReservation(); // 테이블 데이터
    //const metricDto = await getStoreMetric(); // 메트릭 데이터

    // 2. 테이블 뷰 업데이트
    const metaSizeDiv = document.getElementById('table-meta-size');
    const reservationTable = document.getElementById('store-reservation-table');

    const tableHeader = `
        <tr> <!-- table header -->
            <th>예약 ID</th>
            <th>예약일</th>
            <th>예약 시간</th>
            <th>예약자 성함</th>
            <th>예약자 연락처</th>
            <th>예약 상태</th>
            <th>예약 승인</th>
            <th>예약 거절</th>
            <th>예약 취소</th>
        </tr> <!-- end of table header -->
    `;

    let tableRowsHtml = tableHeader; // 헤더 먼저 넣어두고 row html 이어붙임.
    dtoList.forEach(dto => {
        let buttonHtml = '';
        let statusHtml = '';

        switch (dto.reservationStatus) {
            case 'RESERVE_PENDING': // 승인 대기
                buttonHtml = `
                    <td><button class="reserve-confirm-button" onclick="confirmReservation(${dto.reservationId})">예약승인</button></td>
                    <td><button class="reserve-refuse-button" onclick="refuseReservation(${dto.reservationId})">예약거절</button></td>
                    <td><button class="reserve-cancel-button disable">예약취소</button></td>
                `;
                statusHtml = '<td>예약 승인 대기</td>';
                break;
            case 'CONFIRMED': // 예약 확정
                buttonHtml = `
                    <td><button class="reserve-confirm-button disable">승인완료</button></td>
                    <td><button class="reserve-refuse-button disable">예약거절</button></td>
                    <td><button class="reserve-cancel-button" onclick="cancelReservation(${dto.reservationId})">예약취소</button></td>
                `;
                statusHtml = '<td style="color: #325BFF;">예약 확정</td>';
                break;
            case 'REFUSED': // 예약 거절
                buttonHtml = `
                    <td><button class="reserve-confirm-button disable">예약승인</button></td>
                    <td><button class="reserve-refuse-button disable">거절완료</button></td>
                    <td><button class="reserve-cancel-button disable">예약취소</button></td>
                `;
                statusHtml = '<td style="color: #E60000;">예약 거절됨</td>';
                break;
            case 'CANCEL_PENDING': // 취소 대기
                buttonHtml = `
                    <td><button class="reserve-confirm-button disable">예약승인</button></td>
                    <td><button class="reserve-refuse-button disable">예약거절</button></td>
                    <td><button class="reserve-cancel-button" onclick="cancelReservation(${dto.reservationId})">예약취소</button></td>
                `;
                statusHtml = '<td style="color: #E60000;">예약 취소 요청</td>';
                break;
            case 'CANCELED': // 취소 완료
                buttonHtml = `
                    <td><button class="reserve-confirm-button disable">예약승인</button></td>
                    <td><button class="reserve-refuse-button disable">예약거절</button></td>
                    <td><button class="reserve-cancel-button disable">취소완료</button></td>
                `;
                statusHtml = '<td style="color: #E60000;">예약 취소</td>';
                break;
        } // end of switch-case

        let rowHtml = `
            <tr>
                <td>${dto.reservationId}</td>
                <td>${dto.reservedDate}</td>
                <td>${dto.reservedTime}</td>
                <td>${dto.memberName}</td>
                <td>${dto.memberPhone}</td>
                ${statusHtml}
                ${buttonHtml}
            </tr>
        `;
        tableRowsHtml += rowHtml;

    }); // end of forEach

    metaSizeDiv.innerHTML = `전체 <hl>${dtoList.length}</hl>건`;
    reservationTable.innerHTML = tableRowsHtml;
}

async function getStoreReservation() {
    const response = await getRequest("http://localhost:9093/v1/store/reserve");
    console.log(response);

    return response.data; // dto list
}
async function getStoreMetric() {
    const response = await getRequest("http://localhost:9093/v1/store/reserve/metric");
    console.log(response);

    return response.data; // dto
}


/* 예약 승인 함수 */
async function confirmReservation(rid) { // rid: reservation id
    const ok = confirm("예약을 승인하시겠습니까?");
    if (ok) {
        await putRequest("http://localhost:9093/v1/store/reserve/confirm?id=" + rid);
    }
    await updateView();
}

/* 예약 거절 함수 */
async function refuseReservation(rid) {  // rid: reservation id
    const ok = confirm("예약을 거절하시겠습니까?");
    if (ok) {
        await putRequest("http://localhost:9093/v1/store/reserve/refuse?id=" + rid);
    }
    await updateView();
}

/* 예약 취소 함수 */
async function cancelReservation(rid) {  // rid: reservation id
    const ok = confirm("예약을 취소하시겠습니까?");
    if (ok) {
        await putRequest("http://localhost:9093/v1/store/reserve/cancel?id=" + rid);
    }
    await updateView();
}

/* axios request */
async function getRequest(endpoint) {
    try {
        const response = await axios.get(endpoint);
        console.log(response);
        return response;
    } catch (error) {
        console.error(error);
        alert("서버와의 통신에 실패했습니다.");
    }
}

async function putRequest(endpoint) {
    try {
        const response = await axios.put(endpoint);
        console.log(response);
    } catch (error) {
        console.error(error);
        alert("서버와의 통신에 실패했습니다.");
    }
}
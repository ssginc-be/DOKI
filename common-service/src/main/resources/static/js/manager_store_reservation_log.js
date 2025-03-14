// layout-manager.js에 API_GATEWAY_HOST 이미 선언되어 있음.

/* 예약 관리 이력 페이지는 테이블이 2개이기 때문에, updateView 함수도 2개로 나뉨 */

/*
    예약 테이블 뷰 업데이트 함수
*/
const MIN_ROW = 8; // 데이터가 8개보다 적어도, row가 최소 8개 렌더링되도록 함
async function updateReservationTableView() {
    console.log("update reservation table view");

    // 1. 데이터 가져오기
    const dtoList = await getStoreReservation(); // 예약 테이블 데이터

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
            <th>예약 내역 조회</th>
        </tr> <!-- end of table header -->
    `;

    let tableRowsHtml = tableHeader; // 헤더 먼저 넣어두고 row html 이어붙임.
    dtoList.forEach(dto => {
        let statusHtml = '';

        switch (dto.reservationStatus) {
            case 'RESERVE_PENDING': // 승인 대기
                statusHtml = '<td>예약 승인 대기</td>';
                break;
            case 'CONFIRMED': // 예약 확정
                statusHtml = '<td style="color: #325BFF;">예약 확정</td>';
                break;
            case 'REFUSED': // 예약 거절
                statusHtml = '<td style="color: #E60000;">예약 거절됨</td>';
                break;
            case 'CANCEL_PENDING': // 취소 대기
                statusHtml = '<td style="color: #E60000;">예약 취소 요청</td>';
                break;
            case 'CANCELED': // 취소 완료
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
                <td><button class="reserve-log-button" onclick="handleLogButtonClicked(${dto.reservationId})">내역조회</button></td>
            </tr>
        `;
        tableRowsHtml += rowHtml;

    }); // end of forEach

    metaSizeDiv.innerHTML = `전체 <hl>${dtoList.length}</hl>건의 예약이 조회되었습니다.`;
    reservationTable.innerHTML = tableRowsHtml;
}


/*
    예약 관리 이력 테이블 뷰 업데이트 함수
*/
async function updateReservationLogTableView(rid) {
    console.log("update reservation log table view");

    // 1. 데이터 가져오기
    const dtoList = await getStoreReservationLog(rid); // 예약 테이블 데이터

    // 2. 테이블 뷰 업데이트
    const metaLabelDiv = document.getElementById('table-meta-label');
    const reservationLogTable = document.getElementById('store-reservation-log-table');

    const tableHeader = `
        <tr> <!-- table header -->
            <th>로그 ID</th>
            <th>API 버전</th>
            <th>예약 방식</th>
            <th>예약 상태</th>
            <th>관리 일시</th>
            <th>관리자</th>
        </tr> <!-- end of table header -->
    `;

    let tableRowsHtml = tableHeader; // 헤더 먼저 넣어두고 row html 이어붙임.
    dtoList.forEach(dto => {
        let statusHtml = '';

        switch (dto.reservationStatus) {
            case 'RESERVE_PENDING': // 승인 대기
                statusHtml = '<td class="no-button">예약 승인 대기</td>';
                break;
            case 'CONFIRMED': // 예약 확정
                statusHtml = '<td class="no-button style="color: #325BFF;">예약 확정</td>';
                break;
            case 'REFUSED': // 예약 거절
                statusHtml = '<td class="no-button style="color: #E60000;">예약 거절됨</td>';
                break;
            case 'CANCEL_PENDING': // 취소 대기
                statusHtml = '<td class="no-button style="color: #E60000;">예약 취소 요청</td>';
                break;
            case 'CANCELED': // 취소 완료
                statusHtml = '<td class="no-button style="color: #E60000;">예약 취소</td>';
                break;
        } // end of switch-case

        let rowHtml = `
            <tr>
                <td class="no-button">${dto.reservationLogId}</td>
                <td class="no-button">${dto.reserveMethodCode}</td>
                <td class="no-button">${dto.reserveMethod}</td>
                ${statusHtml}
                <td class="no-button">${dto.timestamp}</td>
                <td class="no-button">${dto.reserveMethodCode === 'v1' ? memberName : 'SYSTEM'}</td>
            </tr>
        `;
        tableRowsHtml += rowHtml;

    }); // end of forEach

    metaLabelDiv.innerHTML = `예약 <hl>${rid}</hl>번의 이력이 조회되었습니다.`;
    reservationLogTable.innerHTML = tableRowsHtml;
}


// 데이터 가져오는 함수
async function getStoreReservation() { // 예약
    const response = await getRequest(API_GATEWAY_HOST + "/v1/store/reserve");
    console.log(response);

    return response.data; // dto list
}

async function getStoreReservationLog(rid) { // 예약 관리 이력
    const response = await getRequest(API_GATEWAY_HOST + "/v1/store/reserve/log?id=" + rid);
    console.log(response);

    return response.data; // dto list
}

/* 예약 내역 조회 버튼 클릭시 작동하는 함수 */
async function handleLogButtonClicked(rid) {  // rid: reservation id
    await updateReservationLogTableView(rid);
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

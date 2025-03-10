const MIN_ROW = 8; // 데이터가 8개보다 적어도, row가 최소 8개 렌더링되도록 함
async function updateView() {
    console.log("update view");
    // 1. 데이터 가져오기
    const dtoList = await getMyReservation();

    // 2. counter div 업데이트
    let upcomingMetaSizeDiv = document.getElementById('upcoming-table-meta-size');
    let pastMetaSizeDiv = document.getElementById('past-table-meta-size');

    // 3. 테이블 업데이트
    let upcomingTable = document.getElementById('upcoming-reservation');
    let pastTable = document.getElementById('past-reservation');

    // upcoming table에 붙일 새 rows (header 미리 넣음)
    let newUpcomingTableRowsHtml = `
        <tr> <!-- table header -->
            <th>예약 ID</th>
            <th>예약한 팝업스토어</th>
            <th>예약일</th>
            <th>예약 시간</th>
            <th>예약 상태</th>
            <th>예약 취소 요청</th>
        </tr> <!-- end of table header -->
    `;
    // past table에 붙일 새 rows (header 미리 넣음)
    let newPastTableRowsHtml = `
        <tr> <!-- table header -->
            <th>예약 ID</th>
            <th>예약한 팝업스토어</th>
            <th>예약일</th>
            <th>예약 시간</th>
            <th>예약 상태</th>
        </tr> <!-- end of table header -->
    `;

    let upcomingRowsCount = 0;
    dtoList.forEach(dto => {
        let statusHtml = ""; // '예약 상태' html
        let buttonHtml = ""; // '예약 취소 요청' 버튼 html
        let isUpcoming = false; // upcoming table에 들어갈건지의 여부

        switch (dto.reservationStatus) {
            case 'RESERVE_PENDING': // 승인 대기
                statusHtml = `<td>승인 대기</td>`;
                buttonHtml = `<button class="reserve-cancel-button" onclick="cancelReservation(${dto.reservationId})">예약취소</button>`;
                isUpcoming = true; ++upcomingRowsCount;
                break;
            case 'CONFIRMED': // 예약 확정
                statusHtml = `<td>예약 확정</td>`;
                buttonHtml = `<button class="reserve-cancel-button" onclick="cancelReservation(${dto.reservationId})">예약취소</button>`;
                isUpcoming = true; ++upcomingRowsCount;
                break;
            case 'REFUSED': // 예약 거절 -> 지난 예약 테이블로
                statusHtml = `<td class="no-button">예약 거절</td>`;
                break;
            case 'CANCEL_PENDING': // 취소 대기
                statusHtml = `<td style="color: #E60000;">예약 취소 대기</td>`;
                buttonHtml = `<button class="reserve-cancel-button disable">요청완료</button>`;
                isUpcoming = true; ++upcomingRowsCount;
                break;
            case 'CANCELED': // 취소 완료 -> 지난 예약 테이블로
                statusHtml = `<td class="no-button" style="color: #E60000;">취소 완료</td>`;
                break;
        }

        // 3-1. 경우에 따라 나누어 row html 생성
        if (isUpcoming) { // 다가오는 예약
            let rowHtml = `
                <tr>
                    <td>${dto.reservationId}</td>
                    <td>${dto.storeName}</td>
                    <td>${dto.reservedDate}</td>
                    <td>${dto.reservedTime}</td>
                    ${statusHtml}
                    <td>${buttonHtml}</td>
                </tr>
            `;
            newUpcomingTableRowsHtml += rowHtml
        }
        else { // 지난 예약
            let rowHtml = `
                <tr>
                    <td>${dto.reservationId}</td>
                    <td>${dto.storeName}</td>
                    <td>${dto.reservedDate}</td>
                    <td>${dto.reservedTime}</td>
                    ${statusHtml}
                </tr>
            `;
            newPastTableRowsHtml += rowHtml
        }
    }); // end of forEach

    // meta size div 교체
    upcomingMetaSizeDiv.innerHTML = `총 <hl>${upcomingRowsCount}</hl>건의 예약 결과가 있습니다.`;
    pastMetaSizeDiv.innerHTML = `총 <hl>${dtoList.length - upcomingRowsCount}</hl>건의 예약 결과가 있습니다.`;

    // 3-2. table에 rows html 붙여넣기
    upcomingTable.innerHTML = newUpcomingTableRowsHtml;
    pastTable.innerHTML = newPastTableRowsHtml;
}

async function getMyReservation() {
    const response = await getRequest("http://localhost:9093/v1/member/reserve");
    console.log(response);

    return response.data; // dto list
}

/* 예약취소 버튼 이벤트 */
async function cancelReservation(rid) {  // rid: reservation id
    const ok = confirm("예약을 취소하시겠습니까?");
    if (ok) {
        await getRequest("http://localhost:9093/v1/member/reserve/cancel?id=" + rid);
    }
    // await으로 응답 기다린 후 view 업데이트
    updateView();
}

/* axios request */
async function getRequest(endpoint) {
    const response = await axios.get(endpoint);
    console.log(response);
    return response;
}
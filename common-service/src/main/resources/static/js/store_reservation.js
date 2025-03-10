/*
    date picker 설정 코드: 예약 날짜를 입력받기 위해 date picker 사용
*/
flatpickr.localize(flatpickr.l10ns.ko); // 언어 변경

const today = new Date().setHours(0, 0, 0, 0);

flatpickrConfig = {
    enableTime: false, // 시간 속성 사용 여부
    dateFormat: "Y-m-d", // 달력 입력 포맷
    local: 'ko', // 언어 설정
    minDate: storeStartDate, // 최소 선택 가능 날짜
    maxDate: storeEndDate, // 최대 선택 가능 날짜
    inline: true
};

function showEntryDates(event) {
    const selectedDate = event.value;

    // 확인창에 선택한 예약 일자 띄우기
    const checkDiv = document.getElementById("check-data-date");
    checkDiv.innerText = selectedDate;

    // 해당 일자에 대한 예약 가능 시간 버튼 표시
    showEntryTimes(storeId, selectedDate);

    console.log('selected date:', selectedDate); // logging
}

// 특정 예약 가능 일자 선택 시 하단에 예약 가능 시간 버튼 표시하는 함수
async function showEntryTimes(storeId, selectedDate) {
    // 선택한 일자에 대한 예약 가능 시간 조회
    const entryTimeList = await getRequest(`http://localhost:9093/v1/store/entry?id=${storeId}&date=${selectedDate}`);
    console.log(entryTimeList);

    // 기존의 '예약 일자를 선택해주세요.' or '예약 가능한 시간이 없습니다.' 텍스트 div 제거
    const noButtonDiv = document.getElementById('reserve-time-nobutton');
    if (noButtonDiv != null) noButtonDiv.remove();

    if (entryTimeList.length === 0) { // A. 예약 가능 시간이 없을 경우
        // 기존에 렌더링된 버튼이 있으면 제거
        const boxDiv = document.getElementById('reserve-time-box');
        boxDiv.innerHTML = '';

        // 예약 시간 컨테이너 div
        const reserveTimeContainerDiv = document.getElementById('reserve-time-container');

        // '예약 가능한 시간이 없습니다.' 텍스트 div 생성
        const newNoButtonDiv = document.createElement("div");
        newNoButtonDiv.classList.add('reserve-time-nobutton');
        newNoButtonDiv.id = 'reserve-time-nobutton';
        newNoButtonDiv.innerText = '예약 가능한 시간이 없습니다.';
        reserveTimeContainerDiv.appendChild(newNoButtonDiv);

    }
    else { // B. 예약 가능 시간이 있을 경우 - 예약 가능 시간 버튼 표시

        // 예약 가능 시간 버튼 생성
        // reserve-time-box div
        const boxDiv = document.getElementById('reserve-time-box');

        // createElement로 처리하면 설정할게 많아서 코드가 길어질 것 같아 innerHTML로 처리
        let buttonHTML = '';
        entryTimeList.forEach(entry => {
            let newButtonDiv = entry.entryStatus === 'OPEN' ?
                `<div class="reserve-time-button" data-value="${entry.entryTime}" onclick="setReservationTime(this)"><span>${entry.entryTime}</span></div>`
                :
                `<div class="reserve-time-button closed"><span>${entry.entryTime}</span></div>`;
            buttonHTML += newButtonDiv;
        });
        boxDiv.innerHTML = buttonHTML;
    }
}

// 예약 날짜 버튼 클릭 시 작동하는 함수 - legacy 방식, 현 버전에서 사용하지 않음.
function setReservationDate(event) {
    // 버튼 스타일 변경
    const dateButtons = Array.from(document.getElementsByClassName('reserve-date-button'));
    dateButtons.forEach(btn => {
        btn.classList.remove('selected');
    });
    event.classList.add('selected');

    // 예약 일자 가져오기
    const selectedDate = event.dataset.value;

    // 확인창에 선택한 예약 일자 띄우기
    const checkDiv = document.getElementById("check-data-date");
    checkDiv.innerText = selectedDate;

    console.log('selected date:', selectedDate); // logging
}

function setReservationTime(event) {
    // 버튼 스타일 변경
    const timeButtons = Array.from(document.getElementsByClassName('reserve-time-button'));
    timeButtons.forEach(btn => {
        btn.classList.remove('selected');
    });
    event.classList.add('selected');

    // 예약 시간 가져오기
    const selectedTime = event.dataset.value;

    // 확인창에 선택한 예약 시간 띄우기
    const checkDiv = document.getElementById("check-data-time");
    checkDiv.innerText = selectedTime;

    console.log('selected time:', selectedTime); // logging
}

function setHeadCount(event) {
    // 인원 수 가져오기
    const headCount = event.value;

    // 확인창에 기입한 인원 수 띄우기
    const checkDiv = document.getElementById("check-data-headcount");
    checkDiv.innerText = `${headCount}명`;

    // headCount 로그는 스킵
}

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


/* axios request */
async function getRequest(endpoint) {
    try {
        const response = await axios.get(endpoint);
        console.log(response);
        return response.data;
    } catch(error) {
        console.error(error);
        alert("서버와의 통신에 실패했습니다.");
    }
}
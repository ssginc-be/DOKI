// 예약은 서비스가 분리되어 있어서 API Gateway 없이 테스트 불가 (CORS 터짐)
const API_GATEWAY_HOST = "http://localhost:9000"

console.info('예약 방식:', reserveMethod);

/*
    예약 폼 유효성 검사 함수
*/
function checkReserveAvailable() {
    const name = document.getElementById('check-data-name').innerText;
    const phone = document.getElementById('check-data-phone').innerText;
    const date = document.getElementById('check-data-date').innerText;
    const time = document.getElementById('check-data-time').innerText;
    const headcount = document.getElementById('check-data-headcount').innerText.replace('명', '');

    const phoneRegex = /^\d{11}$/;
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
    const timeRegex = /^([01]?[0-9]|2[0-3]):([0-5]?[0-9])$/;

    const NAME_CHECK = name.length > 0;
    const PHONE_CHECK = phoneRegex.test(phone);
    const DATE_CHECK = dateRegex.test(date);
    const TIME_CHECK = timeRegex.test(time);
    const HEADCOUNT_CHECK = headcount > 0;

    // logging
    console.log('name:', name, ' / NAME_CHECK:', NAME_CHECK);
    console.log('phone:', phone, ' / PHONE_CHECK:', PHONE_CHECK);
    console.log('date:', date, ' / DATE_CHECK:', DATE_CHECK);
    console.log('time:', time, ' / TIME_CHECK:', TIME_CHECK);
    console.log('headcount:', headcount, ' / HEADCOUNT_CHECK:', HEADCOUNT_CHECK);

    return NAME_CHECK && PHONE_CHECK && DATE_CHECK && TIME_CHECK && HEADCOUNT_CHECK;
}

function controlReserveButton() {
    const button = document.getElementById('reserve-button');

    if (checkReserveAvailable()) {
        button.classList.add('active');
        button.onclick = reserve;
    }
    else {
        button.classList.remove('active');
        button.onclick = null;
    }
}

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

    // 기존에 선택한 예약 시간이 있다면 초기화
    const prevCheckTimeDiv = document.getElementById('check-data-time');
    prevCheckTimeDiv.innerText = '-';

    // 해당 일자에 대한 예약 가능 시간 버튼 표시
    showEntryTimes(storeId, selectedDate);

    console.log('selected date:', selectedDate); // logging

    // 예약 버튼 활성화 여부 판단
    controlReserveButton();
}

// 특정 예약 가능 일자 선택 시 하단에 예약 가능 시간 버튼 표시하는 함수
async function showEntryTimes(storeId, selectedDate) {
    // 선택한 일자에 대한 예약 가능 시간 조회
    const entryTimeList = await getRequest(`${API_GATEWAY_HOST}/v1/store/entry?id=${storeId}&date=${selectedDate}`);
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
                `<div id="${entry.reservationEntryId}" class="reserve-time-button" data-value="${entry.entryTime}" onclick="setReservationTime(this)"><span>${entry.entryTime}</span></div>`
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

    // 예약 버튼 활성화 여부 판단
    controlReserveButton();
}

function setHeadCount(event) {
    // 인원 수 가져오기
    const headCount = event.value;

    // 확인창에 기입한 인원 수 띄우기
    const checkDiv = document.getElementById("check-data-headcount");
    checkDiv.innerText = headCount > 0 ? `${headCount}명` : '-';

    // headCount 로그는 스킵

    // 예약 버튼 활성화 여부 판단
    controlReserveButton();
}

/***************************************************************************
    예약 요청 함수: V1, V2 방식 구분하여 request
        1. V1 예약 성공하면 결과를 alert하고 나의 예약으로 이동
        2. V1 예약 실패하면 alert만 하고 페이지 이동은 없음.
        3. V2 예약은 alert 없이 바로 나의 예약으로 이동
*/
async function reserve() {
    // 유효성 검사
    if (!checkReserveAvailable()) {
        alert('기입 정보가 유효하지 않아 예약할 수 없습니다.');
        return;
    }

    // request body 생성
    const date = document.getElementById('check-data-date').innerText;
    const time = document.getElementById('check-data-time').innerText;
    const headcount = document.getElementById('check-data-headcount').innerText.replace('명', '');

    // string -> number 변환 안해도 됨.
    const body = {
        entryId: document.querySelector('.reserve-time-button.selected').id,
        memberCode: memberCode,
        storeId: storeId,
        reservedDateTime: date + ' ' + time,
        headcount: headcount
    };

    // 예약 방식은 Thymeleaf에서 초기화 함.
    // 지정된 방식으로 예약 API request
    const ok = confirm("예약하시겠습니까?");
    if (ok) {
        try {
            if (reserveMethod === 'V1') { // 직접승인
                await postRequest(`${API_GATEWAY_HOST}/v1/reserve`, body);
                location.href = `${API_GATEWAY_HOST}/member/reserve`;
            }
            else if (reserveMethod === 'V2') { // 자동승인
                await postRequest(`${API_GATEWAY_HOST}/v2/reserve`, body);
                location.href = `${API_GATEWAY_HOST}/member/reserve`;
            }
            else { // reserveMethod 값이 없거나 이상함.
                console.warn('reserveMethod 값이 유효하지 않습니다.');
            }

        } catch (error) { // API 호출 오류
            alert("서버와의 통신에 실패했습니다.");
            console.error(error);
        }
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
    } catch (error) {
        console.error(error);
        // alert("서버와의 통신에 실패했습니다.");
        throw error;
    }
}

async function postRequest(endpoint, requestBody) {
    try {
        const response = await axios.post(endpoint, requestBody);
        console.log(response);
        return response.data;
    } catch (error) {
        console.error(error);
        // alert("서버와의 통신에 실패했습니다.");
        throw error;
    }
}
/*
    date picker 설정 코드: 예약 날짜를 입력받기 위해 date picker 사용
*/
flatpickr.localize(flatpickr.l10ns.ko); // 언어 변경

const today = new Date().setHours(0, 0, 0, 0);

flatpickrConfig = {
    enableTime: false, // 시간 속성 사용 여부
    dateFormat: "Y-m-d", // 달력 입력 포맷
    local: 'ko', // 언어 설정
    minDate: today, // 최소 선택 가능 날짜
    maxDate: '2025-03-23', // 최대 선택 가능 날짜
    inline: true
};

function showEntryDates(event) {
    const selectedDate = event.value;

    // 확인창에 선택한 예약 일자 띄우기
    const checkDiv = document.getElementById("check-data-date");
    checkDiv.innerText = selectedDate;

    console.log('selected date:', selectedDate); // logging
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
const API_GATEWAY_HOST = "http://localhost:9093";

/* 등록시 체크하는 유효성 검사 필드 */
let CHECK_NAME = false;
let CHECK_CATEGORY = false;
let CHECK_BRANCH = false;
let CHECK_STORE_AT = false;
let CHECK_METHOD = false;
let CHECK_SHORT_DESC = false;
let CHECK_LONG_DESC = false;
let CHECK_START_DATE = false;
let CHECK_END_DATE = false;
let CHECK_START_TIME = false;
let CHECK_END_TIME = false;
let CHECK_CAPACITY = false;
let CHECK_GAP = false;
let CHECK_IMAGE = false;

/* 등록시 접근하는 폼 데이터 */
let selectedCategoryIndices = [];
let selectedReserveMethod = null;
let selectedReserveGap = null;
let storeImages = [];
let mainThumbIdx = -1;

/*
    date picker 설정 코드
*/
flatpickr.localize(flatpickr.l10ns.ko); // 언어 변경

const today = new Date().setHours(0, 0, 0, 0);

flatpickrDateConfig = {
    enableTime: false, // 시간 속성 사용 여부
    dateFormat: "Y-m-d", // 달력 입력 포맷
    local: 'ko', // 언어 설정
    inline: false
};

flatpickrTimeConfig = {
    enableTime: true, // 시간 속성 사용 여부
    noCalendar: true, // 시간만 입력
    dateFormat: "H:i", // 입력 포맷
    minuteIncrement: 30, // 시간 단위 (30분)
    time_24hr: true,
    local: 'ko', // 언어 설정
    inline: false
};


/*****************************************************************
    유효성 검사 함수 -> 순수하게 호출 시점에서 체크만 하고, 다른 도메인 수행하면 안됨.
*/
// 팝업스토어 이름
const checkStoreName = event => { CHECK_NAME = event.value.length > 0; };
// 팝업스토어 카테고리
const checkStoreCategory = (event) => { CHECK_CATEGORY = selectedCategoryIndices.length > 0; };
// 지점
const checkBranch = event => { CHECK_BRANCH = event.value !== '선택'; };
// 장소
const checkStoreAt = event => { CHECK_STORE_AT = event.value.length > 0; };
// 예약 방식
const checkReserveMethod = () => { CHECK_METHOD = selectedReserveMethod != null; };
// 팝업스토어 한 줄 설명
const checkStoreShortDesc = event => { CHECK_SHORT_DESC = event.value.length > 0; };
// 팝업스토어 상세 설명
const checkStoreLongDesc = event => { CHECK_LONG_DESC = event.value.length > 0; };
// 운영 시작일
const checkStartDate = event => { CHECK_START_DATE = event.value.length > 0; };
// 운영 종료일
const checkEndDate = event => { CHECK_END_DATE = event.value.length > 0; };
// 운영 시작 시간
const checkStartTime = event => { CHECK_START_TIME = event.value.length > 0; };
// 운영 종료 시간
const checkEndTime = event => { CHECK_END_TIME = event.value.length > 0; };
// 예약 시간 단위
const checkReserveGap = () => { CHECK_GAP = selectedReserveGap != null; };
// 시간별 최대 정원
const checkCapacity = event => { CHECK_CAPACITY = event.value > 0; }
// 등록 이미지
const checkStoreImage = () => {
    // 대표 이미지 인덱스는 최소 0이고, 이미지 리스트의 length-1 보다 클 수 없음
    CHECK_IMAGE = storeImages.length > 0 && 0 <= mainThumbIdx && mainThumbIdx < storeImages.length;
}


/*****************************************************************
    버튼 클릭 핸들링 함수
*/
// 팝업스토어 카테고리
function setCategory(event) {
    // 버튼 스타일 변경
    if (event.classList.contains('selected')) {
        event.classList.remove('selected');
        selectedCategoryIndices = selectedCategoryIndices.filter(item => item != event.value); // !== 안됨
    }
    else {
        event.classList.add('selected');
        selectedCategoryIndices.push(Number(event.value)); // string -> number
    }
    console.log('selectedCategoryIndices:', selectedCategoryIndices) // logging
    checkStoreCategory(event);
}

// 예약 방식
function setMethod(event) {
    // 버튼 스타일 변경
    const methodButtons = Array.from(document.getElementsByClassName('reserve-method-button'));
    methodButtons.forEach(btn => {
        btn.classList.remove('selected');
    });
    event.classList.add('selected');

    // 예약 방식 가져오기
    selectedReserveMethod = event.value;
    console.log('selectedReserveMethod:', selectedReserveMethod); // logging
    checkReserveMethod();
}

// 예약 시간 단위
function setGap(event) {
    // 버튼 스타일 변경
    const gapButtons = Array.from(document.getElementsByClassName('reserve-gap-button'));
    gapButtons.forEach(btn => {
        btn.classList.remove('selected');
    });
    event.classList.add('selected');

    // 예약 시간 단위 가져오기
    selectedReserveGap = event.value;
    console.log('selectedReserveGap:', selectedReserveGap); // logging
    checkReserveGap();
}


/*****************************************************************
    시간별 최대 정원 유효성 검사 함수
 */
function checkAvailableCapacity() {
    const capacityInput = document.getElementById('capacity-input');
    const capacity = capacityInput.value;

    if (capacity.length > 0) {
        if (capacity < 1) {
            alert("시간별 정원은 1명보다 작을 수 없습니다.");
            capacityInput.value = 1;
        }
    }
}


/*****************************************************************
    시작일, 종료일 / 시작 시간, 종료 시간 유효성 검사 함수
        1. 둘 중 하나라도 비어있으면 검사를 하지 않음.
        2. 둘 다 기입되어있으면 검사를 진행함.
*/
function checkAvailableDate() {
    const startDateInput = document.getElementById('start-date-input');
    const endDateInput = document.getElementById('end-date-input');
    const startDate = startDateInput.value;
    const endDate = endDateInput.value;
    if (startDate.length > 0 && endDate.length > 0) { // 두 값이 모두 기입됨
        if (new Date(startDate) > new Date(endDate)) {
            alert("종료일은 최소 시작일부터 가능합니다.")
            endDateInput.value = '';
        }
    }
}

function checkAvailableTime() {
    const startTimeInput = document.getElementById('start-time-input');
    const endTimeInput = document.getElementById('end-time-input');
    const startTime = startTimeInput.value;
    const endTime = endTimeInput.value;
    if (startTime.length > 0 && endTime.length > 0) { // 두 값이 모두 기입됨

        const date1 = new Date()
        date1.setHours(startTime.split(":")[0]);
        date1.setMinutes(startTime.split(":")[1]);
        date1.setSeconds("00");

        const date2 = new Date();
        date2.setHours(endTime.split(":")[0]);
        date2.setMinutes(endTime.split(":")[1]);
        date2.setSeconds("00");

        console.log('date1, date2:', date1, date2);

        if (date1 >= date2) {
            alert("종료 시간은 시작 시간보다 이전이거나 동일할 수 없습니다.")
            endTimeInput.value = '';
        }
    }
}


/*****************************************************************
    이미지 업로드 버튼 클릭시 작동하는 함수
        1. preview 보여주기
        2. 이미지 리스트에 file 추가 및 대표 이미지 idx 설정
*/
function showPreview(e) {
    const uploadedFiles = e.target.files;
    console.log(uploadedFiles); // logging
    if (uploadedFiles.length > 5) {
        alert("이미지는 5장까지 업로드 가능합니다.");
        return;
    }

    // 이미지 업로드 유효성 검사를 통과하면
    const previewArea = document.getElementById('store-image-bottom');
    storeImages = uploadedFiles;
    console.log('storeImages:', storeImages); // logging

    let childrenHtml = ""; // previewArea 안에 붙여넣을 자식 div들
    mainThumbIdx = 0; // 업로드 시 기본적으로 첫번째 이미지를 대표로 지정
    Array.from(storeImages).forEach((image, idx) => {
        const imageUrl = URL.createObjectURL(image);
        childrenHtml += idx === 0 ? // 대표 프레임을 구분해서 렌더링
            `<div class="store-image-box selected" data-value="${idx}" onclick="changeMainThumbIdx(this)"><div id="main-selected-chip" class="main-selected-chip">대표</div><img src="${imageUrl}" alt="이미지 로드 실패"></div>` :
            `<div class="store-image-box" data-value="${idx}" onclick="changeMainThumbIdx(this)"><img src="${imageUrl}" alt="이미지 로드 실패"></div>`;
    });
    console.log(childrenHtml);
    previewArea.innerHTML = childrenHtml;

    CHECK_IMAGE = true;
}

function changeMainThumbIdx(event) {
    const selectedIdx = event.getAttribute('data-value');
    console.log('selected image idx:', selectedIdx);

    // 이미지 프레임 view 업데이트
    const boxDivs = Array.from(document.getElementsByClassName('store-image-box'));
    // 대표 표시 chip 업데이트
    document.getElementById('main-selected-chip').remove(); // 기존 chip 제거
    boxDivs.forEach((box, idx) => {
        box.classList.remove('selected');
        if (idx == selectedIdx) { // === 안됨.
            let newChipDiv = document.createElement('div');
            newChipDiv.classList.add("main-selected-chip");
            newChipDiv.id = 'main-selected-chip';
            newChipDiv.innerText = '대표';
            box.insertBefore(newChipDiv, box.firstChild);
        }
    });
    event.classList.add('selected');

    // global: 대표 이미지 idx 변경
    mainThumbIdx = selectedIdx;
}


/*****************************************************************
    하단 등록하기 버튼 클릭시 작동하는 함수
*/
async function register() {
    const ok = confirm('팝업스토어를 등록하시겠습니까?');
    if (!ok) return;

    const requestBodyJson = {
        storeName: document.getElementById('store-name-input').value,
        categoryList: selectedCategoryIndices,
        storeBranch: document.getElementById('branch').value,
        storeAt: document.getElementById('store-at-input').value,
        storeShortDesc: document.getElementById('short-desc-input').value,
        storeLongDesc: document.getElementById('long-desc-input').value,

        storeStartDate: document.getElementById('start-date-input').value,
        storeEndDate: document.getElementById('end-date-input').value,
        reserveMethod: selectedReserveMethod,

        storeStartTime: document.getElementById('start-time-input').value,
        storeEndTime: document.getElementById('end-time-input').value,
        reserveGap: selectedReserveGap,
        capacity: document.getElementById('capacity-input').value,

        thumbIdx: mainThumbIdx
    };

    const formData = new FormData();
    formData.append('json', new Blob([JSON.stringify(
        requestBodyJson
    )], {
        type: "application/json"
    }));
    Array.from(storeImages).forEach(image => formData.append('image', image));

    // formData logging
    for (let item of formData.entries()) {
        console.log(item[0] + ": " + item[1]);
    }

    // register
    try {
        await postRequest(API_GATEWAY_HOST + "/v1/store/registration",
            formData,
            {headers: {"Content-type": "multipart/form-data"}}
        );
        alert('등록이 완료되었습니다.');
        location.href = '/';
    } catch (error) {
        alert("서버와의 통신에 실패했습니다.");
    }
}

/*****************************************************************
    하단 폼 초기화 버튼 클릭시 작동하는 함수
*/
function resetForm() {
    const ok = confirm('등록 폼을 초기화하시겠습니까?\n작성중인 내용이 저장되지 않습니다.');
    if (ok) {
        location.href = '/';
    }
}


/*****************************************************************
    global: 등록하기 버튼 활성화 가능한지 검사하는 함수
*/
function checkConfirmAvailable() {
    const available = CHECK_NAME && CHECK_CATEGORY && CHECK_BRANCH&& CHECK_STORE_AT && CHECK_METHOD &&
        CHECK_SHORT_DESC && CHECK_LONG_DESC && CHECK_START_DATE && CHECK_END_DATE &&
        CHECK_START_TIME && CHECK_END_TIME && CHECK_CAPACITY && CHECK_GAP && CHECK_IMAGE;

    // console.log("CHECK_NAME:", CHECK_NAME);
    // console.log("CHECK_CATEGORY:", CHECK_CATEGORY);
    // console.log("CHECK_BRANCH:", CHECK_BRANCH);
    // console.log("CHECK_STORE_AT:", CHECK_STORE_AT);
    // console.log("CHECK_METHOD:", CHECK_METHOD);
    // console.log("CHECK_SHORT_DESC:", CHECK_SHORT_DESC);
    // console.log("CHECK_LONG_DESC:", CHECK_LONG_DESC);
    // console.log("CHECK_START_DATE:", CHECK_START_DATE);
    // console.log("CHECK_END_DATE:", CHECK_END_DATE);
    // console.log("CHECK_START_TIME:", CHECK_START_TIME);
    // console.log("CHECK_END_TIME:", CHECK_END_TIME);
    // console.log("CHECK_CAPACITY:", CHECK_CAPACITY);
    // console.log("CHECK_GAP:", CHECK_GAP);
    // console.log("CHECK_IMAGE:", CHECK_IMAGE);

    console.log('register available:', available);
    const button = document.getElementById('register-button');

    if (available) {
        button.classList.add('active');
        button.onclick = register;
    }
    else {
        button.classList.remove('active');
        button.onclick = null;
    }
}



/* axios request */
async function getRequest(endpoint) {
    try {
        const response = await axios.get(endpoint);
        console.log(response);
        return response;
    } catch (error) {
        console.error(error);
        // alert("서버와의 통신에 실패했습니다.");
        throw error;
    }
}

async function postRequest(endpoint, requestBody, header) {
    try {
        const response = await axios.post(endpoint, requestBody, header);
        console.log(response);
        return response;
    } catch (error) {
        console.error(error);
        // alert("서버와의 통신에 실패했습니다.");
        throw error;
    }
}
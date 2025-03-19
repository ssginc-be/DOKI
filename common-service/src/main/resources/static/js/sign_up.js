const API_GATEWAY_HOST = "http://localhost:9000";

/*
    가입시 사용하는 전역 변수
    가입 request 가능한 상태인지 check
*/
let CHECK_EMAIL = false;
let CHECK_PW = false;
let CHECK_PHONE = false;
let CHECK_NAME = false;
let CHECK_GENDER = false;
let CHECK_BIRTH = false;

let SELECTED_GENDER = ""; // MALE or FEMALE


/*
    date picker 설정 코드: 예약 날짜를 입력받기 위해 date picker 사용
*/
flatpickr.localize(flatpickr.l10ns.ko); // 언어 변경

const today = new Date().setHours(0, 0, 0, 0);

flatpickrConfig = {
    enableTime: false, // 시간 속성 사용 여부
    dateFormat: "Y-m-d", // 달력 입력 포맷
    local: 'ko', // 언어 설정
    maxDate: today, // 최대 선택 가능 날짜
    inline: false
};



/* 뒤로 가기, 작성 취소 버튼 클릭시 작동하는 함수 */
function goBack() {
    const ok = confirm("가입을 취소하고 뒤로 가시겠습니까?");
    if (ok) history.back();
}

/* 가입하기 버튼 클릭시 작동하는 함수 */
async function signUp() {
    const ok = confirm("가입하시겠습니까?");
    if (ok) {
        // request body 생성
        let requestBody = {
            member_id: document.getElementById('email-input').value,
            member_pw: document.getElementById('pw-input').value,
            member_name: document.getElementById('name-input').value,
            member_phone: document.getElementById('phone-prefix').value + document.getElementById('phone-input').value,
            member_birth: document.getElementById('birth-input').value,
            member_gender: SELECTED_GENDER
        };

        try {
            await postRequest(API_GATEWAY_HOST + "/v1/auth/sign-up", requestBody);
            alert("가입이 완료되었습니다.");
            location.href = '/'; // 메인 화면으로 이동
        } catch (error) {
            // logging은 postRequest 내부에서 이미 되어있음.
            const errorResponse = error.response.data;
            alert(`[${errorResponse.code}] ${errorResponse.message}`);
        }
    }
}


/*****************************************************************
    [버튼 이벤트 함수]
    이메일 인증 버튼 클릭 시 작동
*/
async function handleEmailCodeButtonClicked() {
    const email = document.getElementById('email-input').value;
    console.log('email input:', email);

    // 인증코드 발송 (여기선 await 하지 않는게 좋음)
    sendEmailCode(email);

    // 이메일 input readonly 처리 + 이메일 인증 버튼 비활성화
    let emailInput = document.getElementById('email-input');
    let emailCheckButton = document.getElementById('email-check-button');
    emailInput.readOnly = true;
    emailCheckButton.innerText = "인증 초기화";
    emailCheckButton.classList.add('active');
    emailCheckButton.onclick = resetEmailVerification; // onclick 함수를 인증 초기화 함수로 변경

    // 이메일 input 하단에 인증코드 입력 input과 [인증 확인] 버튼 표시
    if (!document.getElementById('email-verification-box')) { // 현재 DOM에 없으면
        let newChildDiv = document.createElement('div');
        newChildDiv.classList.add("group-content-box");
        newChildDiv.id = 'email-verification-box';
        let childInnerHtml = `
            <div class="group-content-label"></div>
            <input id="email-code-input" type="text" placeholder="30분 내에 발송된 코드를 입력해주세요." />
            <button class="signup-check-button" id="email-code-check-button" onclick="validateEmailCode()">인증 확인</button>
        `;
        newChildDiv.innerHTML = childInnerHtml;

        let signupGroup = document.getElementById('signup-group-top');
        signupGroup.insertBefore(newChildDiv, signupGroup.children[2]);
    }
}

// API 호출해서 인증 코드 보내보고, 실패하면 view 되돌리는 함수
async function sendEmailCode(email) {
    try {
        await getRequest(API_GATEWAY_HOST + "/v1/auth/email/code?to=" + email);
    } catch (error) {
        // error는 getRequest 안에서 logging 되었음.
        const errorResponse = error.response.data;
        alert(`[${errorResponse.code}] ${errorResponse.message}`);
        // 인증 코드 발송 실패했으므로 view 돌려놓기
        resetEmailVerification();
    }
}

// 이메일 input readonly 풀고 + check button 돌려놓는 함수
function resetEmailVerification() {
    let emailInput = document.getElementById('email-input');
    let emailCheckButton = document.getElementById('email-check-button');
    let boxDiv = document.getElementById('email-verification-box');

    // global check 변수
    CHECK_EMAIL = false;

    emailInput.readOnly = false;
    emailCheckButton.innerText = "이메일 인증";
    emailCheckButton.classList.remove('active');
    emailCheckButton.onclick = handleEmailCodeButtonClicked;
    boxDiv.remove(); // 인증 코드 영역 제거
}


/*****************************************************************
    [버튼 이벤트 함수]
    이메일 인증 확인 버튼 클릭 시 작동
*/
async function validateEmailCode() {
    // 입력된 코드 값 가져오기
    const email = document.getElementById('email-input').value;
    const code = document.getElementById('email-code-input').value;

    // 인증 확인
    try {
        await getRequest(API_GATEWAY_HOST + "/v1/auth/email/validation?email=" + email + "&code=" + code);
        let button = document.getElementById('email-code-check-button');
        button.innerText = "인증 완료";
        button.classList.add("disable");
        button.onclick = null;
        alert("인증되었습니다.");

        // global check 변수
        CHECK_EMAIL = true;
        checkConfirmAvailable(); // alert 확인 누르는 클릭 이벤트는 onclick으로 인식 안돼서 필요함.

    } catch (error) {
        // logging은 getRequest 내부에서 이미 되어있음.
        const errorResponse = error.response.data;
        if (errorResponse.status === 401) {
            alert(`[${errorResponse.code}] ${errorResponse.message}`);
        }
        else {
            alert(error.message);
            // 인증코드 틀린게 아닌 다른 오류라면 readonly 풀고 인증버튼 돌려놓기
            resetEmailVerification();
        }
    }
}




/*****************************************************************
    [버튼 이벤트 함수]
    휴대폰 인증 버튼 클릭 시 작동
*/
async function handlePhoneCodeButtonClicked() {
    const phonePrefix = document.getElementById('phone-prefix')
    const phoneInput = document.getElementById('phone-input')
    const phone = phonePrefix.value + phoneInput.value;
    console.log('phone prefix + input:', phone);

    // 인증코드 발송 (여기도 await 하지 않는게 좋음)
    sendPhoneCode(phone);

    // prefix disable & input readonly 처리 + 휴대폰 인증 버튼 비활성화
    let phoneCheckButton = document.getElementById('phone-check-button');
    phonePrefix.disabled = true;
    phoneInput.readOnly = true;
    phoneCheckButton.innerText = "인증 초기화";
    phoneCheckButton.classList.add('active');
    phoneCheckButton.onclick = resetPhoneVerification; // onclick 함수를 인증 초기화 함수로 변경

    // 휴대폰 번호 input 하단에 인증코드 입력 input과 [인증 확인] 버튼 표시
    if (!document.getElementById('phone-verification-box')) { // 현재 DOM에 없으면
        let newChildDiv = document.createElement('div');
        newChildDiv.classList.add("group-content-box");
        newChildDiv.id = 'phone-verification-box';
        let childInnerHtml = `
            <div class="group-content-label"></div>
            <input id="phone-code-input" type="text" placeholder="5분 내에 발송된 코드를 입력해주세요." />
            <button class="signup-check-button" id="phone-code-check-button" onclick="validatePhoneCode()">인증 확인</button>
        `;
        newChildDiv.innerHTML = childInnerHtml;

        let signupGroup = document.getElementById('signup-group-bottom');
        signupGroup.insertBefore(newChildDiv, signupGroup.children[2]);
    }
}

// API 호출해서 인증 코드 보내보고, 실패하면 view 되돌리는 함수
async function sendPhoneCode(phone) {
    try {
        await getRequest(API_GATEWAY_HOST + "/v1/auth/phone/code?to=" + phone);
    } catch (error) {
        // error는 getRequest 안에서 logging 되었음.
        const errorResponse = error.response.data;
        alert(`[${errorResponse.code}] ${errorResponse.message}`);
        // 인증 코드 발송 실패했으므로 view 돌려놓기
        resetPhoneVerification();
    }
}


/*****************************************************************
    [버튼 이벤트 함수]
    휴대폰 인증 확인 버튼 클릭 시 작동
*/
async function validatePhoneCode() {
    // 입력된 코드 값 가져오기
    const phonePrefix = document.getElementById('phone-prefix').value;
    const phoneInput = document.getElementById('phone-input').value;
    const phone = phonePrefix + phoneInput;
    const code = document.getElementById('phone-code-input').value;

    try {
        await getRequest(API_GATEWAY_HOST + "/v1/auth/phone/validation?phone=" + phone + "&code=" + code);
        let button = document.getElementById('phone-code-check-button');
        button.innerText = "인증 완료";
        button.classList.add("disable");
        button.onclick = null;
        alert("인증되었습니다.");

        // global check 변수
        CHECK_PHONE = true;
        checkConfirmAvailable(); // alert 확인 누르는 클릭 이벤트는 onclick으로 인식 안돼서 필요함.

    } catch (error) {
        // logging은 getRequest 내부에서 이미 되어있음.
        const errorResponse = error.response.data;
        if (errorResponse.status === 401) {
            alert(`[${errorResponse.code}] ${errorResponse.message}`);
        }
        else {
            alert(error.message);
            // 인증코드 틀린게 아닌 다른 오류라면 readonly 풀고 인증버튼 돌려놓기
            resetPhoneVerification();
        }
    }
}


function resetPhoneVerification() {
    let phonePrefix = document.getElementById('phone-prefix');
    let phoneInput = document.getElementById('phone-input');
    let phonelCheckButton = document.getElementById('phone-check-button');
    let boxDiv = document.getElementById('phone-verification-box');

    // global check 변수
    CHECK_PHONE = false;

    phonePrefix.disabled = false;
    phoneInput.readOnly = false;
    phonelCheckButton.innerText = "휴대폰 인증";
    phonelCheckButton.classList.remove('active');
    phonelCheckButton.onclick = handlePhoneCodeButtonClicked;
    boxDiv.remove(); // 인증 코드 영역 제거
}


/*****************************************************************
    [입력 이벤트 함수]
    input 타이핑 시 작동
*/
function checkEmailValid(event) {
    let button = document.getElementById('email-check-button');
    // 이메일 포맷 검사
    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (emailPattern.test(event.value)) {
        button.classList.remove('disable');
        button.onclick = handleEmailCodeButtonClicked;
    }
    else {
        button.classList.add('disable');
        button.onclick = null;
    }
}

function checkPasswordValid(event) {
    // 8자 이상 숫자/대소문자/특수문자 모두 포함 조건에 부합하는지 검사
    let pwInput = event.value;
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+={}\[\]:;"'<>,.?/-]).{8,}$/;

    let pwReInput = document.getElementById('pw-re-input');
    let pwCheckDiv = document.getElementById('pw-check');
    if (passwordPattern.test(pwInput)) {
        pwReInput.placeholder = '비밀번호를 재입력해주세요.';
        pwReInput.readOnly = false;
        pwReInput.oninput = checkPasswordMatch;
        pwCheckDiv.style.visibility = 'unset';
    }
    else {
        pwReInput.placeholder = '8자 이상 숫자/대소문자/특수문자를 모두 포함해야 합니다.';
        pwReInput.readOnly = true;
        pwReInput.oninput = null;
        pwCheckDiv.style.visibility = 'hidden';
    }
}

function checkPasswordMatch(event) {
    // 비밀번호 재입력 값이 기존과 동일한지 검사
    let pwInput = document.getElementById('pw-input').value;
    let pwReInput = event.target.value;
    let pwCheckDiv = document.getElementById('pw-check');
    console.log(pwInput, pwReInput)

    if (pwInput === pwReInput) {
        pwCheckDiv.classList.add('ok');
        pwCheckDiv.innerText = '비밀번호 일치';
        // global check 변수
        CHECK_PW = true;
    }
    else {
        pwCheckDiv.classList.remove('ok');
        pwCheckDiv.innerText = '비밀번호 불일치';
        // global check 변수
        CHECK_PW = false;
    }
}

function checkPhoneValid(event) {
    // 8자리 숫자 여부 검사
    const digitPattern = /^\d{8}$/;
    const button = document.getElementById('phone-check-button');
    
    if (digitPattern.test(event.value)) {
        button.classList.remove('disable');
        button.onclick = handlePhoneCodeButtonClicked;
    }
    else {
        button.classList.add('disable');
        button.onclick = null;
    }
}

function checkMemberNameValid(event) {
    // 8자 이상 숫자/대소문자/특수문자 모두 포함 조건에 부합하는지 검사
    let nameInput = event.value;
    console.log('member name:', nameInput);

    CHECK_NAME = nameInput.length > 0;
}

function handleGenderButtonClicked(event) {
    // 버튼 view 업데이트
    const genderButtons = Array.from(document.getElementsByClassName('gender-button'));
    genderButtons.forEach(btn => {
        btn.classList.remove('active');
    });
    event.classList.add('active');
    
    // 성별 버튼은 한 번 클릭하면 해제 불가함.
    CHECK_GENDER = true;

    console.log('selected gender:', event.value);
    SELECTED_GENDER = event.value === '남자' ? 'MALE' : 'FEMALE';
}

function checkMemberBirthValid(event) {
    // 8자 이상 숫자/대소문자/특수문자 모두 포함 조건에 부합하는지 검사
    let birthInput = event.value;
    console.log('selected birth:', birthInput);

    // date picker로 input 넣으므로 날짜 포맷인지의 유효성 검증은 필요 없음.
    // 단, 백 단에선 날짜 포맷 유효성 검사 필요함.
    CHECK_BIRTH = birthInput.length > 0;
}

/*
    Global listener -> 가입하기 버튼 컨트롤
*/
document.addEventListener('input', (event) => {
    checkConfirmAvailable();
});

document.addEventListener('click', (event) => {
    checkConfirmAvailable();
});

function checkConfirmAvailable() {
    let confirmButton = document.getElementById('signup-confirm-button');
    console.log('CHECK_EMAIL', CHECK_EMAIL);
    let confirmAvailable = CHECK_EMAIL && CHECK_PW && CHECK_PHONE && CHECK_NAME && CHECK_GENDER && CHECK_BIRTH;

    if (confirmAvailable) {
        confirmButton.classList.add('active');
        confirmButton.onclick = signUp;
    }
    else {
        confirmButton.classList.remove('active');
        confirmButton.onclick = null;
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

async function postRequest(endpoint, requestBody) {
    try {
        const response = await axios.post(endpoint, requestBody);
        console.log(response);
        return response;
    } catch (error) {
        console.error(error);
        // alert("서버와의 통신에 실패했습니다.");
        throw error;
    }
}
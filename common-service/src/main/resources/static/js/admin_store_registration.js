function setMethod(event) {
    // 버튼 스타일 변경
    const methodButtons = Array.from(document.getElementsByClassName('reserve-method-button'));
    methodButtons.forEach(btn => {
        btn.classList.remove('selected');
    });
    event.classList.add('selected');

    // 예약 방식 가져오기
    const buttonText = event.innerText;
    let methodType = null;
    if (buttonText === '직접승인') methodType = 'V1';
    else if (buttonText === '자동승인') methodType = 'V2';
    else alert('잘못된 접근입니다.');

    console.log('selected method type:', methodType); // logging
}

function setCategory(event) {
    // 버튼 스타일 변경
    if (event.classList.contains('selected')) {
        event.classList.remove('selected');
    }
    else {
        event.classList.add('selected');
    }
}

function setGap(event) {
    // 버튼 스타일 변경
    const gapButtons = Array.from(document.getElementsByClassName('reserve-gap-button'));
    gapButtons.forEach(btn => {
        btn.classList.remove('selected');
    });
    event.classList.add('selected');

    // 예약 방식 가져오기
    const buttonText = event.innerText;
    let gap = null;
    if (buttonText === '30분') gap = '30';
    else if (buttonText === '1시간') gap = '60';
    else alert('잘못된 접근입니다.');

    console.log('selected gap:', gap); // logging
}
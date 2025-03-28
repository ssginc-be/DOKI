// layout-member.js에 API_GATEWAY_HOST 이미 선언되어 있음.


function loadPage(event) {
    // keyword is a global variable
    const pageIdx = event.innerText;
    location.href = `${API_GATEWAY_HOST}/search?q=${keyword}&page=${pageIdx}`;
}


function gotoStoreInfo(storeId) {
    location.href = `${API_GATEWAY_HOST}/store?id=${storeId}`;
}


/* 카테고리 버튼 클릭 시 view 처리 */
async function updateCategoryView(categoryName, categoryId, pageIdx) {
    console.log('selected category:', categoryName);

    clearUrlAddress();

    // 1. 상단 메인 타이틀 텍스트 변경
    let titleDiv = document.getElementById('category-title');
    titleDiv.innerText = categoryName + ' 팝업스토어';

    // 2. 데이터 가져오기
    const page = await getStoreListByCategory(categoryId, pageIdx);
    const storeList = page.data;

    // 3. 검색 결과 개수 텍스트 변경
    let countDiv = document.getElementById('store-count');
    countDiv.innerHTML = `총 <hl>${page.totalElements}</hl>개의 ${categoryName} 팝업스토어`;

    // 4. store-grid-container의 모든 자식 div를 response 데이터로 교체
    let containerDiv = document.getElementById('store-grid-container');

    let newStoreMetaDivHtml = '';
    storeList.forEach(store => {
        let storeDiv = `
<div class="store-meta" onclick="gotoStoreInfo(${store.storeId})">
    <div class="store-thumbnail"><img src="${store.storeMainThumbnail}"></div>
    <div class="store-text-box">
        <div class="store-name">${store.storeName}</div>
        <div class="store-desc">${store.storeShortDesc}</div>
        <div class="store-period">${store.storeStartDate} ~ ${store.storeEndDate}</div>
    </div>
</div>
`;
        newStoreMetaDivHtml += storeDiv;
    });
    containerDiv.innerHTML = newStoreMetaDivHtml;

    // 5. 페이지네이션 div 교체 (style은 동일하지만 onclick시 트리거되는 함수가 다름)
    let boxDiv = document.getElementById('page-button-box');

    let newPageButtonDivHTML = '';
    for (let i = 0; i < page.totalPages; ++i) {
        let buttonDiv = pageIdx === i + 1 ?
            `<div class="page-button-selected">${pageIdx}</div>`
            :
            `<div class="page-button" onclick="updateCategoryView('${categoryName}', ${categoryId}, ${i+1})">${i+1}</div>`;
        newPageButtonDivHTML += buttonDiv;
    }
    boxDiv.innerHTML = newPageButtonDivHTML;
}

/* 팝업스토어 카테고리 목록 조회 */
async function getStoreListByCategory(categoryId, pageIdx) {
    let endpoint = `${API_GATEWAY_HOST}/v1/store/category?id=${categoryId}`;
    if (pageIdx != null) endpoint += `&page=${pageIdx}`;

    const response = await getRequest(endpoint); // 데이터 가져오기
    console.log(response);

    return response.data; // Page<Store>
}


/* axios request */
async function getRequest(endpoint) {
    try {
        const response = await axios.get(endpoint);
        return response;
    } catch (error) {
        console.error(error)
    }
}

/* clear url address */
function clearUrlAddress() {
    // 맨 위로 스크롤
    window.scrollTo(0, 0);

    // 주소창 조작
    // const nextURL = `http://localhost:9093/category?id=${categoryId}&page=${pageIdx}`;
    const nextURL = API_GATEWAY_HOST; // 새로고침시
    window.history.replaceState({}, '신세계백화점 팝업스토어 행사', nextURL);
}
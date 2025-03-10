function goBack() {
    const ok = confirm("가입을 취소하고 뒤로 가시겠습니까?");
    if (ok) history.back();
}
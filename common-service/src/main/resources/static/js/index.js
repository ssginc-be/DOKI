const AUTH_SERVICE = "http://localhost:9093/v2/auth"

async function sign_in(memberId, memberPw) {
    try {
        const res = await axios.post(AUTH_SERVICE + "/sign-in", {
            member_id: memberId,
            member_pw: memberPw
        });
    } catch (error) {
        console.log(error);
    }
}

async function sign_out() {
    try {
        const res = await axios.get(AUTH_SERVICE + "/sign-out");
    } catch (error) {
        console.log(error);
    }
}

function load_page(idx) {
    location.href = `http://localhost:9093?page=${idx-1}`;
}

function goto_store_info(id) {
    location.href = `http://localhost:9093/store?id=${id}`;
}
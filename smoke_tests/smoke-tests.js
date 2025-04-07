import http from 'k6/http';
import { check } from 'k6';


// export const options = {
//     // tlsAuth: [
//     //     {
//     //         domains: ['your-mtls-enabled-domain.com'], // Specify the domain(s) requiring mTLS
//     //         cert: open('./client.crt'),             // Path to your client certificate file (PEM format)
//     //         key: open('./client.key'),               // Path to your client private key file (PEM format)
//     //     },
//         // You can add more tlsAuth configurations for different domains if needed
//     // ],
//     // vus: 1,
// };

const get_urls = [
    'https://www.google.com',
];

// export function setup() {
//     // Simulate a login process to get a token
//     const loginRes = http.post('https://your-auth.com/login', JSON.stringify({
//         username: 'testuser',
//         password: 'testpassword',
//     }), { headers: { 'Content-Type': 'application/json' } });
//
//     if (loginRes.status !== 200) {
//         console.error('Login failed!');
//         return null;
//     }
//
//     const token = JSON.parse(loginRes.body).access_token;
//     return { token: token };
// }


export function mainSmokeTest() {
    // if (!data || !data.token) {
    //     return; // Skip test if login failed
    // }
    // const token = hmppsAuthGateway.getClientToken()
    // const params = {
    //     headers: {
    //         'Authorization': `Bearer ${token}`,
    //     },
    // };
    get_urls.forEach((url) => {
        const res = http.get(url);
        check(res, {'status code MUST be 200': (res) => res.status === 200})

        // sleep(0.1);
    });
}

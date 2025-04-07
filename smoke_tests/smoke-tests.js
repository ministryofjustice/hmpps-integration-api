import http from 'k6/http';
import { check } from 'k6';


const get_urls = [
    'https://www.google.com',
];


export function mainSmokeTest() {

    get_urls.forEach((url) => {
        const res = http.get(url);
        check(res, {'status code MUST be 200': (res) => res.status === 200})

    });
}

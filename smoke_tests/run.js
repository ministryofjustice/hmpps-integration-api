import {Rate} from 'k6/metrics'

export { mainSmokeTest } from './smoke-tests.js'

export const errorRate = new Rate('errors')

const ExecutionOptions_Scenarios = getScenarios()

export const options = {
    scenarios: ExecutionOptions_Scenarios,
    thresholds: {
        checks: ['rate==1'], // http errors should be less than 1%
    },
}

export function getScenarios() {
    return {
        smokeTest: {
            executor: 'shared-iterations',
            exec: 'mainSmokeTest',
            vus: 1,
            iterations: 1,
            maxDuration: '30s',
        },
    }
}

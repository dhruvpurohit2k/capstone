import { devResponses } from '../features/dev/schemas/devResponses.ts'
import { apiRequest } from './client.ts'

export type DevResource = keyof typeof devResponses

export function listDevRecords<T extends DevResource>(token: string, resource: T) {
  return apiRequest({ url: `/dev/${resource}` }, devResponses[resource].list, token)
}

export function getDevRecord<T extends DevResource>(token: string, resource: T, id: number) {
  return apiRequest({ url: `/dev/${resource}/${id}` }, devResponses[resource].byId, token)
}

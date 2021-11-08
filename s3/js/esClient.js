import { Client as EsClient } from '@elastic/elasticsearch';

export const INDEX = process.env.INDEX;

export const esClient: EsClient = new EsClient({
  nodes: `${process.env.ES_HOST}`,
  requestTimeout: 5000,
});

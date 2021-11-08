import { mapEs } from './mapEs';
import { LOGGER } from '';
import { esClient, INDEX } from './esClient';
import { uploadToS3 } from './uploadToS3';



export const handler = async () => {

  try {
    const allRecords = await fetchLocations();
    LOGGER.info(`Fetched  ${allRecords.length} from elastic search`);
    const stations = allRecords.map((hit) => mapEs(hit._source));
    await uploadToS3(stations);
    return {
      statusCode: 0,
      message: 'Success',
    };
  } catch (e){
    LOGGER.error('error');
    throw e;
  }
};

const fetchLocations = async () => {
  const allRecords = [] as any;

  const response = await esClient.search({
    index: INDEX,
    body: createESQuery(),
    scroll: '10s',
    size: 1000,
  });
  let hits = response.body.hits;
  let scrollId = response.body._scroll_id;
  while (hits && hits.hits.length) {
    allRecords.push(...hits.hits);
    const scrollResponse = await esClient.scroll({
      scroll_id: scrollId,
      scroll: '10s',
    });
    scrollId = scrollResponse.body._scroll_id;
    hits = scrollResponse.body.hits;
  }
  return allRecords;
};

const createESQuery = (): anyEsObject => {
  return {
    query: {
      bool: {
        must: [
          {
            match: {
              owners: 'abc',
            },
          },
        ],
      },
    },
  };
};
export type anyEsObject = {
  [key: string]: any;
};



export const mapEs = async (
  data: ESData,
  
): Promise<Detail> => {
  
  return {
    id: data.id,
    
  };
};

export interface Detail {
id: number,
}

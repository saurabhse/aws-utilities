import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;



public class DownloadEncryptedFile {
	
	public static void main(String[] args) {
		boolean assumeRole = true;
		String header = "a|b|c";
		String [] headerArray = header.split("//|",-1);
		int numHeaderCols = headerArray.length;
		if(args.length != 3) {
			System.exit(1);
		}
		
		String manifestFilePath = args[0];
		String symmetryKey = args[1];
		
		byte[] encodedPrivateKey = Base64.decodeBase64(symmetryKey);
		SecretKey secretKey = new SecretKeySpec(encodedPrivateKey,"AES");
		EncryptionMaterials encryptionMaterials = new EncryptionMaterials(secretKey);
		BasicAWSCredentials awsCreds = new BasicAWSCredentials("","");
		AmazonS3EncryptionClient encryptionClient = null;
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setProxyHost("");
		clientConfig.setProxyPort(8080);
		
		if(assumeRole) {
			AWSSecurityTokenServiceClient stsClient = new AWSSecurityTokenServiceClient(awsCreds,clientConfig);
			AssumeRoleRequest assumerequest = new AssumeRoleRequest().withRoleArn("stage").withDurationSeconds(3600).withRoleSessionname("demo");
			AssumeRoleResult assumeResult = stsClient.assumeRole(assumerequest);
			BasicSessionCredentials temporaryCredentials = new BasicSessionCredentials(assumeResult.getCredentials().getAccessKeyId(),assumeResult.getCredentials().getSecretAccessKey(),assumeResult.getCredentials().getsessionToken());
			encryptionClient = new AmazonS3EncryptionClient(
					temporaryCredentials,
					new StaticEncryptionMaterialsProvider(encryptionMaterials),clientConfig,new CryptoConfiguration());
			
		}else {
			encryptionClient = new AmazonS3EncryptionClient(
					awsCreds,
					new StaticEncryptionMaterialsProvider(encryptionMaterials),clientConfig,new CryptoConfiguration());
			
		}
		
		URI s3manifest = new URI(manifestFilePath);
		String manifestBucket = s3manifest.getHost();
		String manifestKey = s3manifest.getPath().substring(1,s3manifest.getPath().length());
		List<String> fileList = null;
		InputStream is = encryptionClient.getObject(manifestBucket,manifestKey).getObjectContent();
		if(is != null) {
			try {
				String manifestFileContentStr = IOUtils.toString(is);
				JSONObject jsonObj = new JSONObject(manifestFileContentStr.trim());
				JSONArray entries = jsonObj.getJSONArray("entries");
				if(entries != null) {
					fileList = new ArrayList<String>(entries.length());
					for(int i = 0 ;i<entries.length();i++) {
						fileList.add(entries.getJSONObject(i).getString("url"));
					}
				}
			}catch(IOException | JSONException e) {
				throw new RuntimeException(e);
			}finally {
				if(is!=null) {
					try {
						is.close();	
					}catch(IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}
}

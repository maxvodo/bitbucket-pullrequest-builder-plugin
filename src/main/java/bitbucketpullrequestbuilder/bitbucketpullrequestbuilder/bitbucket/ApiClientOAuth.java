package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

import org.apache.commons.httpclient.methods.PutMethod;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.model.OAuthRequest;
import org.scribe.model.*;
import org.jenkinsci.plugins.bitbucket.api.BitbucketApi;
import org.jenkinsci.plugins.bitbucket.api.BitbucketApiService;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by maxvodo
 */
public class ApiClientOAuth extends ApiClient {  
  private static final Logger logger = Logger.getLogger(ApiClient.class.getName());
  
  public ApiClientOAuth (
      String username, String password, 
      String owner, String repositoryName, 
      String key, String name
  ) {
    super(username, password, owner, repositoryName, key, name, null);
  }
  
  @Override
  protected String onSend(HttpMethodBase req) {                     
    try {
      Verb v = Verb.TRACE;
      if (req instanceof GetMethod) v = Verb.GET;    
      if (req instanceof PostMethod) v = Verb.POST;
      if (req instanceof PutMethod) v = Verb.PUT;
      if (req instanceof DeleteMethod) v = Verb.DELETE;  
      if (v == Verb.TRACE) throw new ClassNotFoundException();
      
      UsernamePasswordCredentials up = (UsernamePasswordCredentials)this.credentials;
      if (null == up) throw new NullPointerException("credentials");
    
      BitbucketApiService apiService = (BitbucketApiService) new BitbucketApi().createService(new OAuthConfig(up.getUserName(), up.getPassword()));
      OAuthRequest request = new OAuthRequest(v, req.getURI().toString());
            
      if (req instanceof EntityEnclosingMethod) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();      
        ((EntityEnclosingMethod)req).getRequestEntity().writeRequest(stream);
        request.addPayload(stream.toByteArray());
      }    

      Verifier verifier = null;
      Token token = apiService.getAccessToken(OAuthConstants.EMPTY_TOKEN, verifier);
      apiService.signRequest(token, request);

      Response response = request.send();
      return response.getBody();
    
    } catch(Exception e) {
      logger.log(Level.WARNING, "failed send oauth request.", e);
      e.printStackTrace();
    }      

    return null;
  }
  
}

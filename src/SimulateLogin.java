
import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@SuppressWarnings("deprecation")
public class SimulateLogin {

	static HttpClient httpclient = new DefaultHttpClient();
	static HttpResponse response = null;
	static String 	LoginUrl = "http://www.renren.com/PLogin.do";
	static String	LoginEmail = "****************"; 	//账号
	static String 	LoginPassword = "******"; 			//密码
	static String 	UpdateUrl = null;
	static String 	Result = null;
	static String 	HostID = null, _rtk = null, RequestToken = null;
	static String 	Content = "测试"; 					//状态内容
	
	public static void main(String[] args) throws Exception {
		
		SimulateLogin renren = new SimulateLogin();
	
		if ( renren.Login() ) {
			
			renren.ExtractInfo(  );
			renren.Update(  );
			System.out.println("Success");
		}
	}
	
	private boolean Login() {
		
		HttpPost httpost = new HttpPost(LoginUrl);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("email", LoginEmail));
		nvps.add(new BasicNameValuePair("password", LoginPassword));
		
		try {
			httpost.setEntity(new UrlEncodedFormEntity( nvps, "UTF-8" ));
			response = httpclient.execute(httpost);
			EntityUtils.toString(response.getEntity());
			System.out.println(response.getStatusLine());
			String Login_Success = response.getFirstHeader("Location").getValue();
			System.out.println(Login_Success);
			HttpGet httpget = new HttpGet(Login_Success);
			response = httpclient.execute(httpget);
			Result = EntityUtils.toString(response.getEntity(), "UTF-8");
			FileUtils.writeStringToFile(new File("renren.html"), Result, "UTF-8");
			return true;
		} catch(Exception e) {
			System.out.println(e);
		}
		return false;
	}
	
	
	private void ExtractInfo(  ) {
		
		Document Doc = Jsoup.parse(Result);
		
		UpdateUrl = Doc.select("form[class=status-global-publisher]").attr("action");
		//System.out.println(UpdateUrl);
		//System.out.println(Doc.select("form[class=status-global-publisher]"));
		HostID = UpdateUrl.substring(UpdateUrl.lastIndexOf("/")-9, UpdateUrl.lastIndexOf("/"));
		//System.out.println(HostID);
		Element Request_Rtk = Doc.select("script").first();
		
		int _rtkEnd = Request_Rtk.html().indexOf("',env:");
		int _rtkBegin = Request_Rtk.html().indexOf("check_x:")+9;
		_rtk = Request_Rtk.html().substring(_rtkBegin, _rtkEnd);
		//System.out.println(_rtk);
		
		int RequestEnd = Request_Rtk.html().indexOf("',get_check_x:");
		int RequestBegin = Request_Rtk.html().indexOf("check:")+7;
		RequestToken = Request_Rtk.html().substring(RequestBegin, RequestEnd);
		//System.out.println(RequestToken);
	}
	
	private void Update(  ) throws Exception {
		
		HttpPost post = new HttpPost(UpdateUrl);
		
		List<NameValuePair> cp = new ArrayList<NameValuePair>();
		cp.add(new BasicNameValuePair("content", Content));
        	cp.add(new BasicNameValuePair("hostid", HostID));
        	cp.add(new BasicNameValuePair("requestToken", RequestToken));
        	cp.add(new BasicNameValuePair("_rtk", _rtk));
        	cp.add(new BasicNameValuePair("channel", "renren"));
		
		post.setEntity(new UrlEncodedFormEntity(cp, HTTP.UTF_8));
		response = httpclient.execute(post);
			
		System.out.println(response.getStatusLine());
	}
}
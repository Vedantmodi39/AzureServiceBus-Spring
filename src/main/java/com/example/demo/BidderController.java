package com.example.demo;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BidderController {
	
	public static Bidder b1=null;
	
	@PostMapping("/sendd")
	public Bidder sendData(@RequestBody Bidder b)
	{
		
		System.out.println("inside post method of Bidder controller");
		b1= b;
		AzureBusApplication.sendMessage();
		return b1;
		
		
		//return new Bidder("xyzzzz",11214);
	}



	
}

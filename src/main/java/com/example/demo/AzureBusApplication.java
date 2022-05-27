package com.example.demo;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.google.gson.Gson;


@SpringBootApplication
public class AzureBusApplication {
    
	
	      

      
      

		static String connectionString = "Endpoint=sb://temp1-service-bus.servicebus.windows.net/;SharedAccessKeyName=send-sub-key;SharedAccessKey=LPptPOqpj1pyJz6yN2dTyH6CnIzTPyxuyIQDw5u/lMk=";
		static String topicName = "temp-topic";
		static String subName = "temp-sub";


		
		static void sendMessage()
		{
		// create a Service Bus Sender client for the queue
			
			Gson gson = new Gson();
		       String json = gson.toJson(BidderController.b1);
		
			
		ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
		.connectionString(connectionString)
		.sender()
		.topicName(topicName)
		.buildClient();

		System.out.println("biderr data"+json);

		// send one message to the topic
		senderClient.sendMessage(new ServiceBusMessage(json));
		
		System.out.println("Sent a single message to the topic: " + topicName);
		}

		static List<ServiceBusMessage> createMessages()
		{
		// create a list of messages and return it to the caller
		ServiceBusMessage[] messages = {
//		new ServiceBusMessage("First message"),
//		new ServiceBusMessage("Second message"),
//		new ServiceBusMessage("Third message")
		};
		return Arrays.asList(messages);
		}

		static void sendMessageBatch()
		{
		// create a Service Bus Sender client for the topic
		ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
				
		.connectionString(connectionString)
		.sender()
		.topicName(topicName)
		.buildClient()
		;
		
	



		// Creates an ServiceBusMessageBatch where the ServiceBus.
		ServiceBusMessageBatch messageBatch = senderClient.createMessageBatch();



		// create a list of messages
		List<ServiceBusMessage> listOfMessages = createMessages();



		// We try to add as many messages as a batch can fit based on the maximum size and send to Service Bus when
		// the batch can hold no more messages. Create a new batch for next set of messages and repeat until all
		// messages are sent.
		for (ServiceBusMessage message : listOfMessages) {
		if (messageBatch.tryAddMessage(message)) {
		continue;
		}



		// The batch is full, so we create a new batch and send the batch.
		senderClient.sendMessages(messageBatch);
		System.out.println("Sent a batch of messages to the topic: " + topicName);



		// create a new batch
		messageBatch = senderClient.createMessageBatch();



		// Add that message that we couldn't before.
		if (!messageBatch.tryAddMessage(message)) {
		System.err.printf("Message is too large for an empty batch. Skipping. Max size: %s.", messageBatch.getMaxSizeInBytes());
		}
		}



		if (messageBatch.getCount() > 0) {
		senderClient.sendMessages(messageBatch);
		System.out.println("Sent a batch of messages to the topic: " + topicName);
		}



		//close the client
		senderClient.close();
		}



		// handles received messages
		static void receiveMessages() throws InterruptedException
		{
		CountDownLatch countdownLatch = new CountDownLatch(1);



		// Create an instance of the processor through the ServiceBusClientBuilder
		ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
		.connectionString(connectionString)
		.processor()
		.topicName(topicName)
		.subscriptionName(subName)
		.processMessage(AzureBusApplication :: processMessage )
		.processError(context -> processError(context, countdownLatch))
		.buildProcessorClient();



		System.out.println("Starting the processor");
		processorClient.start();



		TimeUnit.SECONDS.sleep(10);
		System.out.println("Stopping and closing the processor");
		processorClient.close();
		}

		private static void processMessage(ServiceBusReceivedMessageContext context) {
		ServiceBusReceivedMessage message = context.getMessage();
		System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
		message.getSequenceNumber(), message.getBody());
		}

		private static void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
		System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
		context.getFullyQualifiedNamespace(), context.getEntityPath());



		if (!(context.getException() instanceof ServiceBusException)) {
		System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
		return;
		}



		ServiceBusException exception = (ServiceBusException) context.getException();
		ServiceBusFailureReason reason = exception.getReason();



		if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
		|| reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
		|| reason == ServiceBusFailureReason.UNAUTHORIZED) {
		System.out.printf("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
		reason, exception.getMessage());



		countdownLatch.countDown();
		} else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
		System.out.printf("Message lock lost for message: %s%n", context.getException());
		} else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
		try {
		// Choosing an arbitrary amount of time to wait until trying again.
		TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
		System.err.println("Unable to sleep for period of time");
		}
		} else {
		System.out.printf("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
		reason, context.getException());
		}
		}



		public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(AzureBusApplication.class, args);
			

		
	//	public static Bidder bc=BidderController.b1;
		
	
		sendMessageBatch();
		receiveMessages();
		}




		

		
		
		
		
		
		
		
		
		
		
		
		
	}



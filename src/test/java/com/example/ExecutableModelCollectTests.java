package com.example;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.drools.modelcompiler.ExecutableModelProject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;

public class ExecutableModelCollectTests {

	private static final String PROCESS_ID = "example";
	
	//Use Executable Model?
	public static Stream<Arguments> data(){
		return Stream.of(
				Arguments.of(true),
				Arguments.of(false));
	}
	
	@ParameterizedTest
	@MethodSource("data")
	public void withExecutableModel(boolean executable) throws IOException, ClassNotFoundException {
		
		KieBase kbase = loadRules(executable);
		KieSession session = kbase.newKieSession();
		
		Logger logger = mock(Logger.class);

		session.insert(new ClassWithValue(null));
		session.insert(new ClassWithValue(new Object()));
		session.insert(logger);
		
		session.startProcess(PROCESS_ID);
		
		session.fireAllRules();
		
		verify(logger, times(2))
				.info(any());
		
		verify(logger, times(2))
				.debug(any());
	}
	
	private static KieBase loadRules(boolean useExecutable) throws IOException {

		Path resources = Paths.get("src", "test", "resources");
		
		KieServices services = KieServices.Factory.get();
		KieFileSystem kfs = services.newKieFileSystem();

		Resource drl = services.getResources()
				.newFileSystemResource(
						resources.resolve("drl/rules.drl").toFile());
		
		Resource rf = services.getResources()
				.newFileSystemResource(
						resources.resolve("rf/ruleflow.rf").toFile());

		kfs.write(drl);
		kfs.write(rf);
		
		KieBuilder builder = services.newKieBuilder(kfs);
		
		builder = useExecutable ?
				builder.buildAll(ExecutableModelProject.class) :
					builder.buildAll();
		
		if (builder.getResults().hasMessages(Level.ERROR)) {
	        List<Message> errors = builder.getResults().getMessages(Level.ERROR);
	        StringBuilder sb = new StringBuilder("Errors:");
	        for (Message msg : errors) {
	            sb.append("\n  " + prettyBuildMessage(msg));
	        }
	        
	        throw new RuntimeException(sb.toString());
	    }
		
		KieContainer container = services.newKieContainer(
				services.getRepository().getDefaultReleaseId());
		
	    return container.getKieBase();
	}
	
	private static String prettyBuildMessage(Message msg) {
	    return "Message: {"
	        + "id="+ msg.getId()
	        + ", level=" + msg.getLevel()
	        + ", path=" + msg.getPath()
	        + ", line=" + msg.getLine()
	        + ", column=" + msg.getColumn()
	        + ", text=\"" + msg.getText() + "\""
	        + "}";
	}
}

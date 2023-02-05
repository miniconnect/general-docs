package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa;

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository.PostRepository;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;

@Command
public class ApplicationCommand implements Runnable {
    
    private final PostRepository postRepository;
    

    @Inject
    public ApplicationCommand(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    
    @Transactional
    @Override
    public void run() {
        try {
            runThrows();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void runThrows() throws Exception {
        ObjectWriter prettyPrinter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        System.out.println(prettyPrinter.writeValueAsString(postRepository.findAll()));
    }

}

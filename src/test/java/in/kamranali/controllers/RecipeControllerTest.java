package in.kamranali.controllers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import in.kamranali.commands.RecipeCommand;
import in.kamranali.domain.Recipe;
import in.kamranali.exceptions.NotFoundException;
import in.kamranali.services.RecipeService;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 6/19/17.
 */
@Ignore
public class RecipeControllerTest {

    @Mock
    RecipeService recipeService;

    RecipeController controller;

    MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        controller = new RecipeController(recipeService);
      //We are using a mocked servlet context (Mocked Dispatcher servlet) to test MVC controllers
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
        		.setControllerAdvice(new ControllerExceptionHandler())
        		.build();
    }

    @Test
    public void testGetRecipe() throws Exception {

        Recipe recipe = new Recipe();
        recipe.setId("1");

        when(recipeService.findById(Mockito.anyString())).thenReturn(Mono.just(recipe));

        mockMvc.perform(get("/recipe/1/show"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/show"))
                .andExpect(model().attributeExists("recipe"));
    }

    @Test
    public void testGetNewRecipeForm() throws Exception {
        mockMvc.perform(get("/recipe/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/recipeform"))
                .andExpect(model().attributeExists("recipe"));
    }

    @Test
    public void testPostNewRecipeForm() throws Exception {
        RecipeCommand command = new RecipeCommand();
        command.setId("2");

        when(recipeService.saveRecipeCommand(Mockito.any())).thenReturn(Mono.just(command));

        mockMvc.perform(post("/recipe")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)   
                .param("id", "")
                .param("description", "some string")
                .param("directions", "My Directions")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/recipe/2/show"));
    }

    @Test
    public void testGetUpdateView() throws Exception {
        RecipeCommand command = new RecipeCommand();
        command.setId("2");

        when(recipeService.findCommandById(Mockito.anyString())).thenReturn(Mono.just(command));

        mockMvc.perform(get("/recipe/1/update"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/recipeform"))
                .andExpect(model().attributeExists("recipe"));
    }
    
    @Test
    public void testDeleteAction() throws Exception {
    	mockMvc.perform(get("/recipe/1/delete"))
    	.andExpect(status().is3xxRedirection())
    	.andExpect(view().name("redirect:/"));
    	
    	verify(recipeService, times(1)).deleteByid(Mockito.anyString());
    }
    
    @Test
    public void testGetRecipeNotFound() throws Exception {
    	
    	when(recipeService.findById(Mockito.anyString())).thenThrow(NotFoundException.class);
    	
    	mockMvc.perform(get("/recipe/1/show"))
    	.andExpect(status().isNotFound())
    	.andExpect(view().name("404error"));
    	
    }
    
    @Test
    public void testPostNewRecipeValidationFail() throws Exception {
        RecipeCommand command = new RecipeCommand();
        command.setId("2");

        when(recipeService.saveRecipeCommand(Mockito.any())).thenReturn(Mono.just(command));

        mockMvc.perform(post("/recipe")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)   
                .param("id", "")
        )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipe"))
                .andExpect(view().name("recipe/recipeform"));
    }
    
}

package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Pet;
import com.mycompany.myapp.repository.PetRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PetResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PetResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_KIND = "AAAAAAAAAA";
    private static final String UPDATED_KIND = "BBBBBBBBBB";

    private static final Integer DEFAULT_AGE = 1;
    private static final Integer UPDATED_AGE = 2;

    private static final String ENTITY_API_URL = "/api/pets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPetMockMvc;

    private Pet pet;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pet createEntity(EntityManager em) {
        Pet pet = new Pet().name(DEFAULT_NAME).kind(DEFAULT_KIND).age(DEFAULT_AGE);
        return pet;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pet createUpdatedEntity(EntityManager em) {
        Pet pet = new Pet().name(UPDATED_NAME).kind(UPDATED_KIND).age(UPDATED_AGE);
        return pet;
    }

    @BeforeEach
    public void initTest() {
        pet = createEntity(em);
    }

    @Test
    @Transactional
    void createPet() throws Exception {
        int databaseSizeBeforeCreate = petRepository.findAll().size();
        // Create the Pet
        restPetMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isCreated());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeCreate + 1);
        Pet testPet = petList.get(petList.size() - 1);
        assertThat(testPet.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPet.getKind()).isEqualTo(DEFAULT_KIND);
        assertThat(testPet.getAge()).isEqualTo(DEFAULT_AGE);
    }

    @Test
    @Transactional
    void createPetWithExistingId() throws Exception {
        // Create the Pet with an existing ID
        pet.setId(1L);

        int databaseSizeBeforeCreate = petRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPetMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllPets() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        // Get all the petList
        restPetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pet.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].kind").value(hasItem(DEFAULT_KIND)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)));
    }

    @Test
    @Transactional
    void getPet() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        // Get the pet
        restPetMockMvc
            .perform(get(ENTITY_API_URL_ID, pet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pet.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.kind").value(DEFAULT_KIND))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE));
    }

    @Test
    @Transactional
    void getNonExistingPet() throws Exception {
        // Get the pet
        restPetMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPet() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        int databaseSizeBeforeUpdate = petRepository.findAll().size();

        // Update the pet
        Pet updatedPet = petRepository.findById(pet.getId()).get();
        // Disconnect from session so that the updates on updatedPet are not directly saved in db
        em.detach(updatedPet);
        updatedPet.name(UPDATED_NAME).kind(UPDATED_KIND).age(UPDATED_AGE);

        restPetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPet.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPet))
            )
            .andExpect(status().isOk());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
        Pet testPet = petList.get(petList.size() - 1);
        assertThat(testPet.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPet.getKind()).isEqualTo(UPDATED_KIND);
        assertThat(testPet.getAge()).isEqualTo(UPDATED_AGE);
    }

    @Test
    @Transactional
    void putNonExistingPet() throws Exception {
        int databaseSizeBeforeUpdate = petRepository.findAll().size();
        pet.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, pet.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPet() throws Exception {
        int databaseSizeBeforeUpdate = petRepository.findAll().size();
        pet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPet() throws Exception {
        int databaseSizeBeforeUpdate = petRepository.findAll().size();
        pet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePetWithPatch() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        int databaseSizeBeforeUpdate = petRepository.findAll().size();

        // Update the pet using partial update
        Pet partialUpdatedPet = new Pet();
        partialUpdatedPet.setId(pet.getId());

        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPet.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPet))
            )
            .andExpect(status().isOk());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
        Pet testPet = petList.get(petList.size() - 1);
        assertThat(testPet.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPet.getKind()).isEqualTo(DEFAULT_KIND);
        assertThat(testPet.getAge()).isEqualTo(DEFAULT_AGE);
    }

    @Test
    @Transactional
    void fullUpdatePetWithPatch() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        int databaseSizeBeforeUpdate = petRepository.findAll().size();

        // Update the pet using partial update
        Pet partialUpdatedPet = new Pet();
        partialUpdatedPet.setId(pet.getId());

        partialUpdatedPet.name(UPDATED_NAME).kind(UPDATED_KIND).age(UPDATED_AGE);

        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPet.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPet))
            )
            .andExpect(status().isOk());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
        Pet testPet = petList.get(petList.size() - 1);
        assertThat(testPet.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPet.getKind()).isEqualTo(UPDATED_KIND);
        assertThat(testPet.getAge()).isEqualTo(UPDATED_AGE);
    }

    @Test
    @Transactional
    void patchNonExistingPet() throws Exception {
        int databaseSizeBeforeUpdate = petRepository.findAll().size();
        pet.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, pet.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPet() throws Exception {
        int databaseSizeBeforeUpdate = petRepository.findAll().size();
        pet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPet() throws Exception {
        int databaseSizeBeforeUpdate = petRepository.findAll().size();
        pet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(pet))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pet in the database
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePet() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        int databaseSizeBeforeDelete = petRepository.findAll().size();

        // Delete the pet
        restPetMockMvc
            .perform(delete(ENTITY_API_URL_ID, pet.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Pet> petList = petRepository.findAll();
        assertThat(petList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

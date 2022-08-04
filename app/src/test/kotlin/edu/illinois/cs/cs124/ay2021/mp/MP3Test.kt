@file:Suppress("MaximumLineLength")

package edu.illinois.cs.cs124.ay2021.mp

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2021.mp.activities.RestaurantActivity
import edu.illinois.cs.cs124.ay2021.mp.models.Preference
import edu.illinois.cs.cs124.ay2021.mp.models.RelatedRestaurants
import edu.illinois.cs.cs124.ay2021.mp.models.Restaurant
import edu.illinois.cs.cs124.ay2021.mp.network.loadPreferences
import edu.illinois.cs.cs124.ay2021.mp.network.loadRestaurants
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

/*
 * This is the MP3 test suite.
 * The code below is used to evaluate your app during testing, local grading, and official grading.
 * You will probably not understand all of the code below, but you'll need to have some understanding of how it works
 * so that you can determine what is wrong with your app and what you need to fix.
 *
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 * You may modify the code below if it is useful during your own local testing, but any changes you make will
 * be lost once you submit.
 * Please keep that in mind, particularly if you see differences between your local scores and your official scores.
 *
 * For more notes on testing, please see the MP0 test suites (MP0Test.java).
 *
 * Version 1, updated 11/17/2021.
 */

// Number of expected restaurants
private const val RESTAURANT_COUNT = 255

// Number of expected preferences
private const val PREFERENCE_COUNT = 45

// Before testing begins, load the restaurant and preferences list so that we have loaded lists for testing purposes
private val restaurants = ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .readValue(loadRestaurants(), object : TypeReference<List<Restaurant>>() {})
    .also {
        check(it.size == RESTAURANT_COUNT) { "Wrong restaurant count" }
    }
private val preferences = ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .readValue(loadPreferences(), object : TypeReference<List<Preference>>() {})
    .also {
        check(it.size == PREFERENCE_COUNT) { "Wrong preference count" }
    }

// Get a copy of the restaurant list for testing Restaurant equality and hashing
private val restaurantsCopy = ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .readValue(loadRestaurants(), object : TypeReference<List<Restaurant>>() {})
    .also {
        check(it.size == RESTAURANT_COUNT) { "Wrong restaurant count" }
    }

// Map between restaurant ID and restaurant, used during testing
private val restaurantMap = restaurantsCopy.map { restaurant -> restaurant.id to restaurant }.toMap()

@Suppress("MaxLineLength")
@RunWith(Enclosed::class)
class MP3Test {

    // Unit tests that don't require simulating the entire app
    @Suppress("LargeClass")
    class UnitTests {
        // Helper method for testGetRelatedInOrder
        private fun testGetRelatedInOrder(
            restaurantID: String,
            relatedSize: Int,
            relatedInOrder: String,
            relatedRestaurants: RelatedRestaurants
        ) {
            val relatedOrdered = if (relatedInOrder == "") {
                listOf()
            } else {
                relatedInOrder.split(",")
            }
            // Create the list of restaurants we're expecting
            val restaurantsOrdered: List<Restaurant>
            try {
                restaurantsOrdered = relatedRestaurants.getRelatedInOrder(restaurantID)
            } catch (e: Exception) {
                // Passing bad restaurantIDs should generate IllegalArgumentExceptions
                if (relatedSize == -1) {
                    assertWithMessage("Should throw IllegalArgumentException")
                        .that(e)
                        .isInstanceOf(IllegalArgumentException::class.java)
                    return
                } else {
                    throw e
                }
            }
            // Bad restaurant IDs should throw and not reach here
            assertWithMessage("Should have thrown exception").that(relatedSize).isNotEqualTo(-1)
            // List of related restaurants should be null
            assertWithMessage("Ordered related list should not be null").that(relatedOrdered).isNotNull()
            // List of related restaurants should match the passed size
            assertWithMessage("Ordered related list is not the correct size")
                .that(restaurantsOrdered.size)
                .isEqualTo(relatedSize)
            // Note that we only check the front of the list here, not the entire list, since otherwise
            // the hardcoded test cases get really awful
            for (i in relatedOrdered.indices) {
                assertWithMessage("Ordered related list is not properly ordered")
                    .that(restaurantsOrdered[i].id)
                    .isEqualTo(relatedOrdered[i])
            }
        }

        // Test whether RelatedRestaurants.getRelatedInOrder works
        @Test(timeout = 1000L)
        @Graded(points = 20)
        @Suppress("LongMethod")
        fun testGetRelatedInOrder() {
            // Initialize the RelatedRestaurants object
            val related = RelatedRestaurants(restaurants, preferences)

            // Test cases generated using the code below
            testGetRelatedInOrder(
                "ce04b72e-dd92-479a-ab7c-0177ae484f6f",
                2,
                "9725bf47-fb9f-4c5b-b114-c4e456e40d93,da567a9b-442a-4ffc-9c7b-e905e10bcba4",
                related
            )
            testGetRelatedInOrder(
                "7a3c27ff-463b-4982-8f2a-ec5ef498f477",
                14,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,cbb5c524-8dc2-45ce-9d17-32504375889d,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "839dbef4-61a1-49bb-8082-bb5eba6c7f0d",
                29,
                "5e41341c-aa14-4347-83c9-9202addac97d,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,cbb5c524-8dc2-45ce-9d17-32504375889d",
                related
            )
            testGetRelatedInOrder("238786f3-1766-405f-a297-5f8f5224ba13", 0, "", related)
            testGetRelatedInOrder("028ab5ab-2d99-4185-92ab-47391fab4aab", 0, "", related)
            testGetRelatedInOrder(
                "ecdcb4fc-df03-4753-adde-0e6088fd67f9",
                11,
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,cbb5c524-8dc2-45ce-9d17-32504375889d,7c66b81e-ef1d-4c1a-8847-b92f8293466b,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "5e41341c-aa14-4347-83c9-9202addac97d",
                62,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,cbb5c524-8dc2-45ce-9d17-32504375889d,77825645-c1a5-4c73-a76c-e56e840bc490,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778",
                related
            )
            testGetRelatedInOrder("a4b90429-cfcc-4831-a8db-a22388238225", 0, "", related)
            testGetRelatedInOrder("d276e998-b654-4b7e-8921-732576b99820", 0, "", related)
            testGetRelatedInOrder("f508b5de-41da-4938-b0e4-372052a53c5c", 0, "", related)
            testGetRelatedInOrder(
                "7f923c86-5a4b-4cac-87e0-6cc88cf27cea",
                7,
                "67083e98-2811-440f-bf06-075542ff24c1,7c66b81e-ef1d-4c1a-8847-b92f8293466b,0a5c264f-839f-4321-8155-0cc5d418aba5,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetRelatedInOrder("150673ce-307f-4697-afc1-f94bf2eb1dc3", 0, "", related)
            testGetRelatedInOrder(
                "f3ebf7cf-4f94-4efa-a0af-654d50ba093b",
                35,
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b,67083e98-2811-440f-bf06-075542ff24c1,c058069c-7c33-4eed-8c95-0e7366db0928,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetRelatedInOrder("5eb9ffb4-d37a-4160-ad0a-acd60d5658bf", 0, "", related)
            testGetRelatedInOrder("0cb36db9-13aa-4cd9-ae0f-3ed97d3ed801", 0, "", related)
            testGetRelatedInOrder("ac718ac2-4ccf-45a8-9a2a-7f97e9642c4a", 0, "", related)
            testGetRelatedInOrder(
                "a95964f6-fe1a-4ef8-8182-5fcf5d65c839",
                8,
                "e7efd4f8-31f3-44f0-8874-12519f39547c,120eb6ac-df37-430d-b019-72d108272dc7,faff5e95-3c10-4d6c-8717-dcfe088dc2f6,c102c13f-a593-407e-90a3-cc198db7ddfb",
                related
            )
            testGetRelatedInOrder(
                "0a5c264f-839f-4321-8155-0cc5d418aba5",
                7,
                "67083e98-2811-440f-bf06-075542ff24c1,7c66b81e-ef1d-4c1a-8847-b92f8293466b,7f923c86-5a4b-4cac-87e0-6cc88cf27cea,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetRelatedInOrder(
                "354ea76e-4f32-4f00-9e4c-ac3a5659af27",
                18,
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,18114bcc-b21b-44e2-819b-56a84b64c2ed,67083e98-2811-440f-bf06-075542ff24c1,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "7154e35e-3f54-4bc3-a1a1-9335b63f18a9",
                34,
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b,cef64bfd-7440-41cc-8863-bf06443726f2,edb0634a-7116-4780-9a25-a853631b69f8,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetRelatedInOrder(
                "cac5439e-658e-4a8b-8942-064c388101f8",
                36,
                "cef64bfd-7440-41cc-8863-bf06443726f2,8181bd2f-3f33-4200-b9af-224a8b8dd537,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "34d0a0eb-6b28-4005-bcdc-e0b45dfba79e",
                23,
                "289c8ae0-6c2e-48bf-89a7-ab7f1f780a06,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,178e4690-cab1-4b1d-bd1a-161b72bdaaf8",
                related
            )
            testGetRelatedInOrder(
                "fe6c9179-b653-4185-b04c-e2445c091fe4",
                5,
                "d54bbb0c-609a-483c-adf9-f10f1f07411a,77825645-c1a5-4c73-a76c-e56e840bc490,362748f3-ece7-4b70-8e5d-2025f5acae41,08b72d2a-e528-4675-81e2-e0a13e8c0f0e",
                related
            )
            testGetRelatedInOrder(
                "0a5c264f-839f-4321-8155-0cc5d418aba5",
                7,
                "67083e98-2811-440f-bf06-075542ff24c1,7c66b81e-ef1d-4c1a-8847-b92f8293466b,7f923c86-5a4b-4cac-87e0-6cc88cf27cea,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetRelatedInOrder(
                "18114bcc-b21b-44e2-819b-56a84b64c2ed",
                14,
                "67083e98-2811-440f-bf06-075542ff24c1,77825645-c1a5-4c73-a76c-e56e840bc490,079965ba-8b88-4f7f-abec-5d75377298f3,354ea76e-4f32-4f00-9e4c-ac3a5659af27",
                related
            )
            testGetRelatedInOrder(
                "44f4f47e-5208-4f81-bdfe-e89a606b29b1",
                1,
                "9247635d-53f9-4231-a4ad-d386bee69f0f",
                related
            )
            testGetRelatedInOrder(
                "44f4f47e-5208-4f81-bdfe-e89a606b29b1",
                1,
                "9247635d-53f9-4231-a4ad-d386bee69f0f",
                related
            )
            testGetRelatedInOrder(
                "a8d4d47a-0ed1-4ef8-9e31-fd52991c1eee",
                26,
                "faff5e95-3c10-4d6c-8717-dcfe088dc2f6,9247635d-53f9-4231-a4ad-d386bee69f0f,cac5439e-658e-4a8b-8942-064c388101f8,a03cfd89-49b8-45c2-aac7-f9370732f525",
                related
            )
            testGetRelatedInOrder(
                "edb0634a-7116-4780-9a25-a853631b69f8",
                39,
                "cbb5c524-8dc2-45ce-9d17-32504375889d,7c66b81e-ef1d-4c1a-8847-b92f8293466b,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,07b903ce-154e-492f-9d3d-c1aaa7c2f460",
                related
            )
            testGetRelatedInOrder(
                "0fe46aff-43ca-4ba7-a375-0c7068419ee5",
                10,
                "e81f1297-373f-4df2-b754-0ca4e363c589,5e41341c-aa14-4347-83c9-9202addac97d,cbb5c524-8dc2-45ce-9d17-32504375889d,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f",
                related
            )
            testGetRelatedInOrder(
                "7fe1cc3b-5048-44dd-9274-fa296ac8c3c7",
                17,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,5e41341c-aa14-4347-83c9-9202addac97d,08b72d2a-e528-4675-81e2-e0a13e8c0f0e,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778",
                related
            )
            testGetRelatedInOrder(
                "689d2091-4329-4455-ac70-67afb849b6d9",
                57,
                "77825645-c1a5-4c73-a76c-e56e840bc490,9d697062-0e78-41fd-8670-20f0eca805d3,67083e98-2811-440f-bf06-075542ff24c1,7c66b81e-ef1d-4c1a-8847-b92f8293466b",
                related
            )
            testGetRelatedInOrder(
                "2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f",
                35,
                "5e41341c-aa14-4347-83c9-9202addac97d,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,e81f1297-373f-4df2-b754-0ca4e363c589,07b903ce-154e-492f-9d3d-c1aaa7c2f460",
                related
            )
            testGetRelatedInOrder(
                "079965ba-8b88-4f7f-abec-5d75377298f3",
                22,
                "77825645-c1a5-4c73-a76c-e56e840bc490,5c437a4b-8f33-4dc3-8670-63952c149754,689d2091-4329-4455-ac70-67afb849b6d9,8d810579-997c-497c-a80e-d6e37b48f081",
                related
            )
            testGetRelatedInOrder(
                "0316dbd6-015f-46c0-9467-07f061e70007",
                16,
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b,9d697062-0e78-41fd-8670-20f0eca805d3,e4b97946-55e2-4003-a077-a5387f723b3a,bb075707-bf11-4c40-bd35-c4bff30e9885",
                related
            )
            testGetRelatedInOrder(
                "079965ba-8b88-4f7f-abec-5d75377298f3",
                22,
                "77825645-c1a5-4c73-a76c-e56e840bc490,5c437a4b-8f33-4dc3-8670-63952c149754,689d2091-4329-4455-ac70-67afb849b6d9,8d810579-997c-497c-a80e-d6e37b48f081",
                related
            )
            testGetRelatedInOrder(
                "d7896727-5aa4-4e87-8076-f36112e8e0da",
                34,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,7c66b81e-ef1d-4c1a-8847-b92f8293466b",
                related
            )
            testGetRelatedInOrder(
                "a3f3197c-904b-40bd-9aed-33a3d2d9a1ab",
                15,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,ee955584-68ee-4c70-bb75-4a24317222aa,02d87226-749e-4577-8101-f7f864a9121c",
                related
            )
            testGetRelatedInOrder(
                "1e5aff7d-157e-43d1-a7b2-60df14ba805a",
                20,
                "cac5439e-658e-4a8b-8942-064c388101f8,a03cfd89-49b8-45c2-aac7-f9370732f525,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetRelatedInOrder(
                "729cf04e-8203-4001-a681-b6191d4011c8",
                23,
                "289c8ae0-6c2e-48bf-89a7-ab7f1f780a06,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,178e4690-cab1-4b1d-bd1a-161b72bdaaf8",
                related
            )
            testGetRelatedInOrder(
                "7f923c86-5a4b-4cac-87e0-6cc88cf27cea",
                7,
                "67083e98-2811-440f-bf06-075542ff24c1,7c66b81e-ef1d-4c1a-8847-b92f8293466b,0a5c264f-839f-4321-8155-0cc5d418aba5,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetRelatedInOrder(
                "f6c71062-7773-4a87-98a2-a9550a9addc5",
                20,
                "cac5439e-658e-4a8b-8942-064c388101f8,a03cfd89-49b8-45c2-aac7-f9370732f525,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetRelatedInOrder(
                "44722219-fbbd-4974-a6aa-dc17c87d967e",
                13,
                "a03cfd89-49b8-45c2-aac7-f9370732f525,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,d11b0a93-c87e-405b-93ec-9fdd0bdb3b49,cbb5c524-8dc2-45ce-9d17-32504375889d",
                related
            )
            testGetRelatedInOrder(
                "a03cfd89-49b8-45c2-aac7-f9370732f525",
                32,
                "e4b97946-55e2-4003-a077-a5387f723b3a,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,cef64bfd-7440-41cc-8863-bf06443726f2,cac5439e-658e-4a8b-8942-064c388101f8",
                related
            )
            testGetRelatedInOrder(
                "44f4f47e-5208-4f81-bdfe-e89a606b29b1",
                1,
                "9247635d-53f9-4231-a4ad-d386bee69f0f",
                related
            )
            testGetRelatedInOrder(
                "cbd00363-0ae0-477f-8c8e-3be787dff703",
                11,
                "214178a5-7dcd-4dea-8049-5b18f24b52b8,7c66b81e-ef1d-4c1a-8847-b92f8293466b,edb0634a-7116-4780-9a25-a853631b69f8,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetRelatedInOrder(
                "9e6a4af1-f65f-40ff-8eff-886536e8ca51",
                11,
                "cac5439e-658e-4a8b-8942-064c388101f8,e7efd4f8-31f3-44f0-8874-12519f39547c,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "9e6a4af1-f65f-40ff-8eff-886536e8ca51",
                11,
                "cac5439e-658e-4a8b-8942-064c388101f8,e7efd4f8-31f3-44f0-8874-12519f39547c,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "362748f3-ece7-4b70-8e5d-2025f5acae41",
                37,
                "77825645-c1a5-4c73-a76c-e56e840bc490,cef64bfd-7440-41cc-8863-bf06443726f2,e4b97946-55e2-4003-a077-a5387f723b3a,9247635d-53f9-4231-a4ad-d386bee69f0f",
                related
            )
            testGetRelatedInOrder(
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b",
                61,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,77825645-c1a5-4c73-a76c-e56e840bc490,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetRelatedInOrder(
                "a8d4d47a-0ed1-4ef8-9e31-fd52991c1eee",
                26,
                "faff5e95-3c10-4d6c-8717-dcfe088dc2f6,9247635d-53f9-4231-a4ad-d386bee69f0f,cac5439e-658e-4a8b-8942-064c388101f8,a03cfd89-49b8-45c2-aac7-f9370732f525",
                related
            )
            testGetRelatedInOrder(
                "c413dcf8-bcc5-4e8c-85ef-6fea73ba4099",
                29,
                "cbb5c524-8dc2-45ce-9d17-32504375889d,5e41341c-aa14-4347-83c9-9202addac97d,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,839dbef4-61a1-49bb-8082-bb5eba6c7f0d",
                related
            )
            testGetRelatedInOrder(
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778",
                48,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,67083e98-2811-440f-bf06-075542ff24c1,7c66b81e-ef1d-4c1a-8847-b92f8293466b,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "443e67fa-6327-4738-8be4-7eb37cad3c59",
                22,
                "cac5439e-658e-4a8b-8942-064c388101f8,a03cfd89-49b8-45c2-aac7-f9370732f525,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetRelatedInOrder(
                "da567a9b-442a-4ffc-9c7b-e905e10bcba4",
                18,
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b,689d2091-4329-4455-ac70-67afb849b6d9,e4b97946-55e2-4003-a077-a5387f723b3a,214178a5-7dcd-4dea-8049-5b18f24b52b8",
                related
            )
            testGetRelatedInOrder(
                "d54bbb0c-609a-483c-adf9-f10f1f07411a",
                14,
                "77825645-c1a5-4c73-a76c-e56e840bc490,362748f3-ece7-4b70-8e5d-2025f5acae41,08b72d2a-e528-4675-81e2-e0a13e8c0f0e,9247635d-53f9-4231-a4ad-d386bee69f0f",
                related
            )
            testGetRelatedInOrder(
                "33fb1f35-bb05-4f68-ae69-2125b083a1b9",
                14,
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,7c66b81e-ef1d-4c1a-8847-b92f8293466b,ecdcb4fc-df03-4753-adde-0e6088fd67f9,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f",
                related
            )
            testGetRelatedInOrder(
                "02fb03a3-d8e9-4ec1-8c76-bac21219e6ba",
                31,
                "77825645-c1a5-4c73-a76c-e56e840bc490,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,7c66b81e-ef1d-4c1a-8847-b92f8293466b,839dbef4-61a1-49bb-8082-bb5eba6c7f0d",
                related
            )
            testGetRelatedInOrder(
                "cac5439e-658e-4a8b-8942-064c388101f8",
                36,
                "cef64bfd-7440-41cc-8863-bf06443726f2,8181bd2f-3f33-4200-b9af-224a8b8dd537,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,77825645-c1a5-4c73-a76c-e56e840bc490",
                related
            )
            testGetRelatedInOrder(
                "34d0a0eb-6b28-4005-bcdc-e0b45dfba79e",
                23,
                "289c8ae0-6c2e-48bf-89a7-ab7f1f780a06,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,178e4690-cab1-4b1d-bd1a-161b72bdaaf8",
                related
            )
            testGetRelatedInOrder(
                "c058069c-7c33-4eed-8c95-0e7366db0928",
                34,
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,7c66b81e-ef1d-4c1a-8847-b92f8293466b,8181bd2f-3f33-4200-b9af-224a8b8dd537,f3ebf7cf-4f94-4efa-a0af-654d50ba093b",
                related
            )
            testGetRelatedInOrder(
                "aa0055c4-a695-4fc3-9d24-c11a8ab6684b",
                5,
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b,77825645-c1a5-4c73-a76c-e56e840bc490,8969d77a-adc8-4f61-83ab-9289b1658249,da567a9b-442a-4ffc-9c7b-e905e10bcba4",
                related
            )
            testGetRelatedInOrder(
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b",
                61,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,77825645-c1a5-4c73-a76c-e56e840bc490,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetRelatedInOrder(
                "079965ba-8b88-4f7f-abec-5d75377298f3",
                22,
                "77825645-c1a5-4c73-a76c-e56e840bc490,5c437a4b-8f33-4dc3-8670-63952c149754,689d2091-4329-4455-ac70-67afb849b6d9,8d810579-997c-497c-a80e-d6e37b48f081",
                related
            )

            // Test bad restaurant IDs
            testGetRelatedInOrder("c301d45a-90e9-4b4e-8cd0-1b04939e7bf5", -1, "", related)
            testGetRelatedInOrder("c7421950-05d8-426d-8ef7-5787b6bb4e02", -1, "", related)

            // How most of the test cases above were generated, using the solution set

            /*
            // How most of the test cases above were generated, using the solution set
            var emptyCount = 0
            var nonEmptyCount = 0
            while (nonEmptyCount < 64) {
                val restaurant = restaurants[random.nextInt(restaurants.size))]
                val relatedOrdered = related.getRelatedInOrder(restaurant.id)
                if (relatedOrdered.isEmpty() && emptyCount++ > 8) {
                    continue
                }
                println(
                    "testGetRelatedInOrder(\"" + restaurant.id + "\", " + relatedOrdered.size + ", \"" + relatedOrdered.stream()
                        .map(Restaurant::id).limit(4).collect(
                            Collectors.joining(",")
                        ) + "\", related);"
                )
                nonEmptyCount++
            }
             */
        }

        // Helper method for testGetConnectedTo
        private fun testGetConnectedTo(
            restaurantID: String,
            connectedSize: Int,
            connectedSubset: String,
            relatedRestaurants: RelatedRestaurants
        ) {
            val connected = if (connectedSubset == "") {
                listOf()
            } else {
                connectedSubset.split(",")
            }
            // Create the subset of connected restaurants we're expected
            val connectedRestaurants: Set<Restaurant?>
            try {
                connectedRestaurants = relatedRestaurants.getConnectedTo(restaurantID)
            } catch (e: Exception) {
                // Bad restaurant IDs should generate IllegalArgumentExceptions
                if (connectedSize == -1) {
                    assertWithMessage("Should throw IllegalArgumentException")
                        .that(e)
                        .isInstanceOf(IllegalArgumentException::class.java)
                    return
                } else {
                    throw e
                }
            }
            // Invalid restaurant IDs should throw and not reach here
            assertWithMessage("Should have thrown exception").that(connectedSize).isNotEqualTo(-1)
            // The connected set should not be null
            assertWithMessage("Connected set should not be null").that(connectedRestaurants).isNotNull()
            // The connected set should have the expected size
            assertWithMessage("Connected set is not the correct size")
                .that(connectedRestaurants.size)
                .isEqualTo(connectedSize)
            // Note that we only check a subset of the full connected set, since checking the entire set
            // would cause the hardcoded test cases below to be even more terrible
            for (otherID in connected) {
                assertWithMessage("Connected set does not contain the correct items")
                    .that(connectedRestaurants.contains(restaurantMap[otherID]))
                    .isTrue()
            }
        }

        // Test whether RelatedRestaurants.getConnectedTo works
        @Test(timeout = 1000L)
        @Graded(points = 20)
        @Suppress("LongMethod")
        fun testGetConnectedTo() {
            // Initialize the RelatedRestaurants object
            val related = RelatedRestaurants(restaurants, preferences)

            // Randomly-generated test cases

            // Randomly-generated test cases
            testGetConnectedTo("ce04b72e-dd92-479a-ab7c-0177ae484f6f", 0, "", related)
            testGetConnectedTo(
                "7a3c27ff-463b-4982-8f2a-ec5ef498f477",
                36,
                "689d2091-4329-4455-ac70-67afb849b6d9,d7896727-5aa4-4e87-8076-f36112e8e0da,07b903ce-154e-492f-9d3d-c1aaa7c2f460,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetConnectedTo(
                "839dbef4-61a1-49bb-8082-bb5eba6c7f0d",
                49,
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,7fe1cc3b-5048-44dd-9274-fa296ac8c3c7,e81f1297-373f-4df2-b754-0ca4e363c589",
                related
            )
            testGetConnectedTo("238786f3-1766-405f-a297-5f8f5224ba13", 0, "", related)
            testGetConnectedTo("028ab5ab-2d99-4185-92ab-47391fab4aab", 0, "", related)
            testGetConnectedTo("ecdcb4fc-df03-4753-adde-0e6088fd67f9", 0, "", related)
            testGetConnectedTo(
                "5e41341c-aa14-4347-83c9-9202addac97d",
                52,
                "0f783417-d41e-496e-9291-d27cb30207d9,9247635d-53f9-4231-a4ad-d386bee69f0f,df205946-1c32-48e8-a8d9-f6cabc914f93,08b72d2a-e528-4675-81e2-e0a13e8c0f0e",
                related
            )
            testGetConnectedTo("a4b90429-cfcc-4831-a8db-a22388238225", 0, "", related)
            testGetConnectedTo("d276e998-b654-4b7e-8921-732576b99820", 0, "", related)
            testGetConnectedTo("f508b5de-41da-4938-b0e4-372052a53c5c", 0, "", related)
            testGetConnectedTo("7f923c86-5a4b-4cac-87e0-6cc88cf27cea", 0, "", related)
            testGetConnectedTo("150673ce-307f-4697-afc1-f94bf2eb1dc3", 0, "", related)
            testGetConnectedTo(
                "f3ebf7cf-4f94-4efa-a0af-654d50ba093b",
                42,
                "2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,77825645-c1a5-4c73-a76c-e56e840bc490,839dbef4-61a1-49bb-8082-bb5eba6c7f0d,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778",
                related
            )
            testGetConnectedTo(
                "7154e35e-3f54-4bc3-a1a1-9335b63f18a9",
                40,
                "2c78d28b-4aa0-4518-8134-81a5ea2a14b4,c102c13f-a593-407e-90a3-cc198db7ddfb,9ece6624-b4c6-4a14-819e-6ef1451c0a4e,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f",
                related
            )
            testGetConnectedTo(
                "cac5439e-658e-4a8b-8942-064c388101f8",
                44,
                "7a3c27ff-463b-4982-8f2a-ec5ef498f477,d7896727-5aa4-4e87-8076-f36112e8e0da,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,689d2091-4329-4455-ac70-67afb849b6d9",
                related
            )
            testGetConnectedTo(
                "a8d4d47a-0ed1-4ef8-9e31-fd52991c1eee",
                14,
                "cac5439e-658e-4a8b-8942-064c388101f8,8181bd2f-3f33-4200-b9af-224a8b8dd537,9247635d-53f9-4231-a4ad-d386bee69f0f,cef64bfd-7440-41cc-8863-bf06443726f2",
                related
            )
            testGetConnectedTo(
                "edb0634a-7116-4780-9a25-a853631b69f8",
                41,
                "c413dcf8-bcc5-4e8c-85ef-6fea73ba4099,c058069c-7c33-4eed-8c95-0e7366db0928,7fe1cc3b-5048-44dd-9274-fa296ac8c3c7,9ece6624-b4c6-4a14-819e-6ef1451c0a4e",
                related
            )
            testGetConnectedTo(
                "0fe46aff-43ca-4ba7-a375-0c7068419ee5",
                28,
                "079965ba-8b88-4f7f-abec-5d75377298f3,7c66b81e-ef1d-4c1a-8847-b92f8293466b,8d810579-997c-497c-a80e-d6e37b48f081,689d2091-4329-4455-ac70-67afb849b6d9",
                related
            )
            testGetConnectedTo(
                "7fe1cc3b-5048-44dd-9274-fa296ac8c3c7",
                42,
                "8d810579-997c-497c-a80e-d6e37b48f081,02fb03a3-d8e9-4ec1-8c76-bac21219e6ba,c102c13f-a593-407e-90a3-cc198db7ddfb,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetConnectedTo(
                "689d2091-4329-4455-ac70-67afb849b6d9",
                51,
                "78502a34-e27f-41b6-8bc6-13c75103dcb5,8d810579-997c-497c-a80e-d6e37b48f081,0fe46aff-43ca-4ba7-a375-0c7068419ee5,faff5e95-3c10-4d6c-8717-dcfe088dc2f6",
                related
            )
            testGetConnectedTo(
                "2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f",
                43,
                "c413dcf8-bcc5-4e8c-85ef-6fea73ba4099,c102c13f-a593-407e-90a3-cc198db7ddfb,8181bd2f-3f33-4200-b9af-224a8b8dd537,7c66b81e-ef1d-4c1a-8847-b92f8293466b",
                related
            )
            testGetConnectedTo(
                "079965ba-8b88-4f7f-abec-5d75377298f3",
                42,
                "e4b97946-55e2-4003-a077-a5387f723b3a,032900f9-6285-42e4-a664-bba64282417f,839dbef4-61a1-49bb-8082-bb5eba6c7f0d,08b72d2a-e528-4675-81e2-e0a13e8c0f0e",
                related
            )
            testGetConnectedTo(
                "0316dbd6-015f-46c0-9467-07f061e70007",
                32,
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,44b53c27-9cec-4986-b0a8-4cd5da83e5e8,edb0634a-7116-4780-9a25-a853631b69f8,689d2091-4329-4455-ac70-67afb849b6d9",
                related
            )
            testGetConnectedTo(
                "079965ba-8b88-4f7f-abec-5d75377298f3",
                42,
                "c413dcf8-bcc5-4e8c-85ef-6fea73ba4099,e3d0291b-1463-4336-9cd2-d2102d98f6d9,cac5439e-658e-4a8b-8942-064c388101f8,0f783417-d41e-496e-9291-d27cb30207d9",
                related
            )
            testGetConnectedTo(
                "d7896727-5aa4-4e87-8076-f36112e8e0da",
                47,
                "07b903ce-154e-492f-9d3d-c1aaa7c2f460,77825645-c1a5-4c73-a76c-e56e840bc490,da567a9b-442a-4ffc-9c7b-e905e10bcba4,a03cfd89-49b8-45c2-aac7-f9370732f525",
                related
            )
            testGetConnectedTo(
                "a3f3197c-904b-40bd-9aed-33a3d2d9a1ab",
                37,
                "7a3c27ff-463b-4982-8f2a-ec5ef498f477,cac5439e-658e-4a8b-8942-064c388101f8,cef64bfd-7440-41cc-8863-bf06443726f2,839dbef4-61a1-49bb-8082-bb5eba6c7f0d",
                related
            )
            testGetConnectedTo(
                "a03cfd89-49b8-45c2-aac7-f9370732f525",
                24,
                "9247635d-53f9-4231-a4ad-d386bee69f0f,7154e35e-3f54-4bc3-a1a1-9335b63f18a9,da567a9b-442a-4ffc-9c7b-e905e10bcba4,0316dbd6-015f-46c0-9467-07f061e70007",
                related
            )
            testGetConnectedTo(
                "362748f3-ece7-4b70-8e5d-2025f5acae41",
                47,
                "cac5439e-658e-4a8b-8942-064c388101f8,9ece6624-b4c6-4a14-819e-6ef1451c0a4e,faff5e95-3c10-4d6c-8717-dcfe088dc2f6,8d810579-997c-497c-a80e-d6e37b48f081",
                related
            )
            testGetConnectedTo(
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b",
                50,
                "a03cfd89-49b8-45c2-aac7-f9370732f525,e81f1297-373f-4df2-b754-0ca4e363c589,d54bbb0c-609a-483c-adf9-f10f1f07411a,07b903ce-154e-492f-9d3d-c1aaa7c2f460",
                related
            )
            testGetConnectedTo(
                "a8d4d47a-0ed1-4ef8-9e31-fd52991c1eee",
                14,
                "77825645-c1a5-4c73-a76c-e56e840bc490,cef64bfd-7440-41cc-8863-bf06443726f2,67083e98-2811-440f-bf06-075542ff24c1,9247635d-53f9-4231-a4ad-d386bee69f0f",
                related
            )
            testGetConnectedTo(
                "c413dcf8-bcc5-4e8c-85ef-6fea73ba4099",
                31,
                "9ece6624-b4c6-4a14-819e-6ef1451c0a4e,08b72d2a-e528-4675-81e2-e0a13e8c0f0e,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,079965ba-8b88-4f7f-abec-5d75377298f3",
                related
            )
            testGetConnectedTo(
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778",
                50,
                "689d2091-4329-4455-ac70-67afb849b6d9,e3d0291b-1463-4336-9cd2-d2102d98f6d9,9ece6624-b4c6-4a14-819e-6ef1451c0a4e,178e4690-cab1-4b1d-bd1a-161b72bdaaf8",
                related
            )
            testGetConnectedTo(
                "da567a9b-442a-4ffc-9c7b-e905e10bcba4",
                36,
                "5e41341c-aa14-4347-83c9-9202addac97d,8307e49e-946c-4ee0-b04b-e80e37296fcc,0316dbd6-015f-46c0-9467-07f061e70007,689d2091-4329-4455-ac70-67afb849b6d9",
                related
            )
            testGetConnectedTo(
                "d54bbb0c-609a-483c-adf9-f10f1f07411a",
                35,
                "9d697062-0e78-41fd-8670-20f0eca805d3,5c437a4b-8f33-4dc3-8670-63952c149754,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,8307e49e-946c-4ee0-b04b-e80e37296fcc",
                related
            )
            testGetConnectedTo(
                "33fb1f35-bb05-4f68-ae69-2125b083a1b9",
                17,
                "8181bd2f-3f33-4200-b9af-224a8b8dd537,02fb03a3-d8e9-4ec1-8c76-bac21219e6ba,d7896727-5aa4-4e87-8076-f36112e8e0da,44b53c27-9cec-4986-b0a8-4cd5da83e5e8",
                related
            )
            testGetConnectedTo(
                "02fb03a3-d8e9-4ec1-8c76-bac21219e6ba",
                49,
                "c058069c-7c33-4eed-8c95-0e7366db0928,079965ba-8b88-4f7f-abec-5d75377298f3,67083e98-2811-440f-bf06-075542ff24c1,cac5439e-658e-4a8b-8942-064c388101f8",
                related
            )
            testGetConnectedTo(
                "cac5439e-658e-4a8b-8942-064c388101f8",
                44,
                "d7896727-5aa4-4e87-8076-f36112e8e0da,df205946-1c32-48e8-a8d9-f6cabc914f93,c058069c-7c33-4eed-8c95-0e7366db0928,7a3c27ff-463b-4982-8f2a-ec5ef498f477",
                related
            )
            testGetConnectedTo(
                "c058069c-7c33-4eed-8c95-0e7366db0928",
                43,
                "a3f3197c-904b-40bd-9aed-33a3d2d9a1ab,da567a9b-442a-4ffc-9c7b-e905e10bcba4,bb075707-bf11-4c40-bd35-c4bff30e9885,839dbef4-61a1-49bb-8082-bb5eba6c7f0d",
                related
            )
            testGetConnectedTo(
                "7c66b81e-ef1d-4c1a-8847-b92f8293466b",
                50,
                "5e41341c-aa14-4347-83c9-9202addac97d,0fe46aff-43ca-4ba7-a375-0c7068419ee5,e3d0291b-1463-4336-9cd2-d2102d98f6d9,d54bbb0c-609a-483c-adf9-f10f1f07411a",
                related
            )
            testGetConnectedTo(
                "079965ba-8b88-4f7f-abec-5d75377298f3",
                42,
                "78502a34-e27f-41b6-8bc6-13c75103dcb5,9ece6624-b4c6-4a14-819e-6ef1451c0a4e,032900f9-6285-42e4-a664-bba64282417f,da567a9b-442a-4ffc-9c7b-e905e10bcba4",
                related
            )
            testGetConnectedTo(
                "178e4690-cab1-4b1d-bd1a-161b72bdaaf8",
                46,
                "bb075707-bf11-4c40-bd35-c4bff30e9885,67083e98-2811-440f-bf06-075542ff24c1,a3f3197c-904b-40bd-9aed-33a3d2d9a1ab,da567a9b-442a-4ffc-9c7b-e905e10bcba4",
                related
            )
            testGetConnectedTo(
                "7154e35e-3f54-4bc3-a1a1-9335b63f18a9",
                40,
                "67083e98-2811-440f-bf06-075542ff24c1,bb075707-bf11-4c40-bd35-c4bff30e9885,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,0316dbd6-015f-46c0-9467-07f061e70007",
                related
            )
            testGetConnectedTo(
                "edb0634a-7116-4780-9a25-a853631b69f8",
                41,
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetConnectedTo(
                "cef64bfd-7440-41cc-8863-bf06443726f2",
                49,
                "c058069c-7c33-4eed-8c95-0e7366db0928,78502a34-e27f-41b6-8bc6-13c75103dcb5,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778,7fe1cc3b-5048-44dd-9274-fa296ac8c3c7",
                related
            )
            testGetConnectedTo(
                "77825645-c1a5-4c73-a76c-e56e840bc490",
                52,
                "0316dbd6-015f-46c0-9467-07f061e70007,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,d11b0a93-c87e-405b-93ec-9fdd0bdb3b49,5e41341c-aa14-4347-83c9-9202addac97d",
                related
            )
            testGetConnectedTo(
                "67083e98-2811-440f-bf06-075542ff24c1",
                52,
                "0f783417-d41e-496e-9291-d27cb30207d9,bb075707-bf11-4c40-bd35-c4bff30e9885,f3ebf7cf-4f94-4efa-a0af-654d50ba093b,c413dcf8-bcc5-4e8c-85ef-6fea73ba4099",
                related
            )
            testGetConnectedTo(
                "2c78d28b-4aa0-4518-8134-81a5ea2a14b4",
                48,
                "7154e35e-3f54-4bc3-a1a1-9335b63f18a9,cbb5c524-8dc2-45ce-9d17-32504375889d,02fb03a3-d8e9-4ec1-8c76-bac21219e6ba,c058069c-7c33-4eed-8c95-0e7366db0928",
                related
            )
            testGetConnectedTo(
                "c413dcf8-bcc5-4e8c-85ef-6fea73ba4099",
                31,
                "44b53c27-9cec-4986-b0a8-4cd5da83e5e8,08b72d2a-e528-4675-81e2-e0a13e8c0f0e,8307e49e-946c-4ee0-b04b-e80e37296fcc,0e7b1fe6-002a-4cab-b07b-0d5ea92c1778",
                related
            )
            testGetConnectedTo(
                "e81f1297-373f-4df2-b754-0ca4e363c589",
                32,
                "9d697062-0e78-41fd-8670-20f0eca805d3,8181bd2f-3f33-4200-b9af-224a8b8dd537,079965ba-8b88-4f7f-abec-5d75377298f3,0fe46aff-43ca-4ba7-a375-0c7068419ee5",
                related
            )
            testGetConnectedTo(
                "9ece6624-b4c6-4a14-819e-6ef1451c0a4e",
                46,
                "df205946-1c32-48e8-a8d9-f6cabc914f93,8181bd2f-3f33-4200-b9af-224a8b8dd537,d7896727-5aa4-4e87-8076-f36112e8e0da,cbb5c524-8dc2-45ce-9d17-32504375889d",
                related
            )
            testGetConnectedTo(
                "d54bbb0c-609a-483c-adf9-f10f1f07411a",
                35,
                "032900f9-6285-42e4-a664-bba64282417f,2c78d28b-4aa0-4518-8134-81a5ea2a14b4,cac5439e-658e-4a8b-8942-064c388101f8,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetConnectedTo(
                "178e4690-cab1-4b1d-bd1a-161b72bdaaf8",
                46,
                "a3f3197c-904b-40bd-9aed-33a3d2d9a1ab,f3ebf7cf-4f94-4efa-a0af-654d50ba093b,8d810579-997c-497c-a80e-d6e37b48f081,9d697062-0e78-41fd-8670-20f0eca805d3",
                related
            )
            testGetConnectedTo(
                "e4b97946-55e2-4003-a077-a5387f723b3a",
                50,
                "8d810579-997c-497c-a80e-d6e37b48f081,2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,da567a9b-442a-4ffc-9c7b-e905e10bcba4,07b903ce-154e-492f-9d3d-c1aaa7c2f460",
                related
            )
            testGetConnectedTo(
                "cac5439e-658e-4a8b-8942-064c388101f8",
                44,
                "44b53c27-9cec-4986-b0a8-4cd5da83e5e8,7c66b81e-ef1d-4c1a-8847-b92f8293466b,9247635d-53f9-4231-a4ad-d386bee69f0f,e3d0291b-1463-4336-9cd2-d2102d98f6d9",
                related
            )
            testGetConnectedTo(
                "8181bd2f-3f33-4200-b9af-224a8b8dd537",
                52,
                "c058069c-7c33-4eed-8c95-0e7366db0928,d11b0a93-c87e-405b-93ec-9fdd0bdb3b49,8307e49e-946c-4ee0-b04b-e80e37296fcc,d54bbb0c-609a-483c-adf9-f10f1f07411a",
                related
            )
            testGetConnectedTo(
                "33fb1f35-bb05-4f68-ae69-2125b083a1b9",
                17,
                "2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,839dbef4-61a1-49bb-8082-bb5eba6c7f0d,44b53c27-9cec-4986-b0a8-4cd5da83e5e8,67083e98-2811-440f-bf06-075542ff24c1",
                related
            )
            testGetConnectedTo(
                "e4b97946-55e2-4003-a077-a5387f723b3a",
                50,
                "2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f,9d697062-0e78-41fd-8670-20f0eca805d3,362748f3-ece7-4b70-8e5d-2025f5acae41,7154e35e-3f54-4bc3-a1a1-9335b63f18a9",
                related
            )
            testGetConnectedTo(
                "032900f9-6285-42e4-a664-bba64282417f",
                49,
                "a3f3197c-904b-40bd-9aed-33a3d2d9a1ab,44b53c27-9cec-4986-b0a8-4cd5da83e5e8,e4b97946-55e2-4003-a077-a5387f723b3a,5c437a4b-8f33-4dc3-8670-63952c149754",
                related
            )
            testGetConnectedTo(
                "2c78d28b-4aa0-4518-8134-81a5ea2a14b4",
                48,
                "5e41341c-aa14-4347-83c9-9202addac97d,07b903ce-154e-492f-9d3d-c1aaa7c2f460,0fe46aff-43ca-4ba7-a375-0c7068419ee5,8307e49e-946c-4ee0-b04b-e80e37296fcc",
                related
            )
            testGetConnectedTo(
                "77825645-c1a5-4c73-a76c-e56e840bc490",
                52,
                "07b903ce-154e-492f-9d3d-c1aaa7c2f460,c413dcf8-bcc5-4e8c-85ef-6fea73ba4099,edb0634a-7116-4780-9a25-a853631b69f8,faff5e95-3c10-4d6c-8717-dcfe088dc2f6",
                related
            )
            testGetConnectedTo(
                "5e41341c-aa14-4347-83c9-9202addac97d",
                52,
                "33fb1f35-bb05-4f68-ae69-2125b083a1b9,e81f1297-373f-4df2-b754-0ca4e363c589,e3d0291b-1463-4336-9cd2-d2102d98f6d9,8181bd2f-3f33-4200-b9af-224a8b8dd537",
                related
            )
            testGetConnectedTo(
                "5e41341c-aa14-4347-83c9-9202addac97d",
                52,
                "9d697062-0e78-41fd-8670-20f0eca805d3,faff5e95-3c10-4d6c-8717-dcfe088dc2f6,e3d0291b-1463-4336-9cd2-d2102d98f6d9,08b72d2a-e528-4675-81e2-e0a13e8c0f0e",
                related
            )
            testGetConnectedTo(
                "d7896727-5aa4-4e87-8076-f36112e8e0da",
                47,
                "cbb5c524-8dc2-45ce-9d17-32504375889d,da567a9b-442a-4ffc-9c7b-e905e10bcba4,178e4690-cab1-4b1d-bd1a-161b72bdaaf8,0316dbd6-015f-46c0-9467-07f061e70007",
                related
            )
            testGetConnectedTo(
                "cef64bfd-7440-41cc-8863-bf06443726f2",
                49,
                "c413dcf8-bcc5-4e8c-85ef-6fea73ba4099,e3d0291b-1463-4336-9cd2-d2102d98f6d9,8307e49e-946c-4ee0-b04b-e80e37296fcc,8181bd2f-3f33-4200-b9af-224a8b8dd537",
                related
            )

            // Test bad restaurant IDs
            testGetConnectedTo("c301d45a-90e9-4b4e-8cd0-1b04939e7bf5", -1, "", related)
            testGetConnectedTo("c7421950-05d8-426d-8ef7-5787b6bb4e02", -1, "", related)

            // How most of the test cases above were generated, using the solution set
            /*
            var emptyCount = 0
            var nonEmptyCount = 0
            while (nonEmptyCount < 64) {
                val restaurant: Restaurant = restaurants[random.nextInt(restaurants.size)]
                val connected = related.getConnectedTo(restaurant.id)
                if (connected.isEmpty() && emptyCount++ > 8) {
                    continue
                }
                val connectedTo = connected.map { it.id }.toList().shuffled()
                println(
                    "testGetConnectedTo(\"" + restaurant.id + "\", " + connected.size + ", \"" + connectedTo.stream()
                        .limit(4).collect(
                            Collectors.joining(",")
                        ) + "\", related);"
                )
                nonEmptyCount++
            }
             */
        }
    }

    // Integration tests that require simulating the entire app
    @RunWith(AndroidJUnit4::class)
    @LooperMode(LooperMode.Mode.PAUSED)
    class IntegrationTests {
        // Helper method for testRestaurantView
        private fun testRestaurantView(restaurantID: String, topRelated: String?, connectedCount: Int) {
            val restaurant = restaurantMap[restaurantID]!!
            var relatedRestaurant: Restaurant? = null
            if (topRelated != null) {
                relatedRestaurant = restaurantMap[topRelated]!!
            }
            // Create and configure the Intent that should launch the Restaurant view activity
            val intent = Intent(
                ApplicationProvider.getApplicationContext(),
                RestaurantActivity::class.java
            )
            intent.putExtra("id", restaurantID)
            // Launch the intent
            val restaurantScenario = ActivityScenario.launch<RestaurantActivity>(intent)
            restaurantScenario.moveToState(Lifecycle.State.CREATED)
            restaurantScenario.moveToState(Lifecycle.State.RESUMED)
            // Check the view for the text that we expect: the restaurant name and cuisine values
            onView(isRoot()).check(matches(hasDescendant(containsText(restaurant.name))))
            onView(isRoot()).check(matches(hasDescendant(containsText(restaurant.cuisine))))
            // Check the view for new text that we expect: the related restaurant name and connected count
            if (relatedRestaurant != null) {
                onView(isRoot()).check(matches(hasDescendant(containsText(relatedRestaurant.name))))
            }
            onView(isRoot()).check(matches(hasDescendant(containsText(connectedCount.toString()))))
        }

        // Test that the Restaurant view activity works correctly and displays the new data
        @Test(timeout = 10000L)
        @Graded(points = 20)
        fun testRestaurantView() {
            // Start the MainActivity so that it can perform any necessary initialization
            startActivity()
            // Test views for a few restaurants
            // Test views for a few restaurants
            testRestaurantView(
                "d54bbb0c-609a-483c-adf9-f10f1f07411a", "77825645-c1a5-4c73-a76c-e56e840bc490", 35
            )
            testRestaurantView(
                "0e7b1fe6-002a-4cab-b07b-0d5ea92c1778", "8181bd2f-3f33-4200-b9af-224a8b8dd537", 50
            )
            testRestaurantView(
                "f5b89ed5-e3bb-41a0-a258-7be3598081de", "cac5439e-658e-4a8b-8942-064c388101f8", 0
            )
            testRestaurantView(
                "2aa4cdb6-6e9d-4c1e-9848-e5a5f7ccf26f", "5e41341c-aa14-4347-83c9-9202addac97d", 43
            )
            testRestaurantView(
                "5c437a4b-8f33-4dc3-8670-63952c149754", "cbb5c524-8dc2-45ce-9d17-32504375889d", 49
            )
            testRestaurantView(
                "5562f96a-b31b-4491-a22d-4f83cf2fafb9", "214178a5-7dcd-4dea-8049-5b18f24b52b8", 0
            )
            testRestaurantView(
                "08b72d2a-e528-4675-81e2-e0a13e8c0f0e", "8181bd2f-3f33-4200-b9af-224a8b8dd537", 47
            )
            testRestaurantView(
                "3ee75667-9832-401f-975b-3a5b22ccc765", "cac5439e-658e-4a8b-8942-064c388101f8", 0
            )
            testRestaurantView("bc9d2fa9-bd61-4aff-8705-6f5a878899a1", null, 0)
        }

        // From the entire course staff: Congratulations on all the hard work you did this semester!
        @Test(timeout = 1000L)
        @Graded(points = 20)
        fun testIsItOver() {
            assertWithMessage("Congratulations on a great semester!").that(true).isTrue()
        }
    }
}

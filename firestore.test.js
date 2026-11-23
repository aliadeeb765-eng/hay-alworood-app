const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_resident_1";
const BOB_UID = "bob_resident_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated access denied across all collections
test("Unauthenticated user: denied read and write on all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb
      .collection("utility_updates")
      .where("visibility", "==", "NEIGHBORHOOD")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("service_providers")
      .where("visibility", "==", "NEIGHBORHOOD")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("community_posts")
      .where("visibility", "==", "NEIGHBORHOOD")
      .get()
  );
});

// 2. Private resident profile & favorites (strict owner isolation / PII protection)
test("Resident profile & favorites: owner can CRUD, other user is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Create own profile (matches HayAlWurudRepository.ensureResidentProfile)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "أبو محمد",
      email: "alice@example.com",
      preferredSector: "الساحة الرئيسية",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Update preferredSector (matches HayAlWurudRepository.updatePreferredSector)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      preferredSector: "منطقة الجبل",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user PII read/write denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      preferredSector: "اختراق",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Favorites subcollection: owner create/list/delete, cross-user denied
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("svc_1")
      .set({
        userId: ALICE_UID,
        serviceId: "svc_1",
        serviceName: "المعلم أبو أحمد",
        specialty: "كهربائي منازل",
        workPhone: "0933112244",
        sector: "الساحة الرئيسية",
        createdAt: now,
      })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
});

// 3. Utility updates: filtered neighborhood query succeeds, unfiltered fails, owner delete works
test("Utility updates: Kotlin payload & filtered query succeed; unfiltered & spoofed writes fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("utility_updates").doc("u_1").set({
      userId: ALICE_UID,
      authorName: "أبو محمد",
      utilityType: "WATER",
      sector: "الساحة الرئيسية",
      status: "AVAILABLE",
      note: "الضخ شغال",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered query matching Kotlin observeUtilityUpdates() succeeds for neighbor Bob
  await assertSucceeds(
    bobDb
      .collection("utility_updates")
      .where("visibility", "==", "NEIGHBORHOOD")
      .get()
  );

  // Unfiltered list query must be rejected
  await assertFails(bobDb.collection("utility_updates").get());

  // Cross-user delete/update denied
  await assertFails(bobDb.collection("utility_updates").doc("u_1").delete());

  // Future timestamp rejected
  await assertFails(
    aliceDb.collection("utility_updates").doc("u_future").set({
      userId: ALICE_UID,
      authorName: "أبو محمد",
      utilityType: "WATER",
      sector: "الساحة الرئيسية",
      status: "AVAILABLE",
      visibility: "NEIGHBORHOOD",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Ghost field / @DocumentId 'id' rejected
  await assertFails(
    aliceDb.collection("utility_updates").doc("u_ghost").set({
      id: "u_ghost",
      userId: ALICE_UID,
      authorName: "أبو محمد",
      utilityType: "WATER",
      sector: "الساحة الرئيسية",
      status: "AVAILABLE",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );
});

// 4. Service providers directory: Kotlin create, filtered query, cross-user protection
test("Service providers: Kotlin create & filtered query succeed; invalid phone & cross-user delete fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("service_providers").doc("sp_1").set({
      userId: ALICE_UID,
      authorName: "أبو محمد",
      name: "أبو طارق السنكري",
      category: "MAINTENANCE",
      specialty: "سباكة وخزانات",
      sector: "الحارة التحتانية",
      workPhone: "0944556677",
      workingHours: "9 صباحاً - 8 مساءً",
      notes: "صيانة سريعة",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    bobDb
      .collection("service_providers")
      .where("visibility", "==", "NEIGHBORHOOD")
      .get()
  );

  // Unfiltered query denied
  await assertFails(bobDb.collection("service_providers").get());

  // Short/invalid workPhone denied
  await assertFails(
    aliceDb.collection("service_providers").doc("sp_bad").set({
      userId: ALICE_UID,
      authorName: "أبو محمد",
      name: "حرفي",
      category: "MAINTENANCE",
      specialty: "سباكة",
      sector: "الساحة الرئيسية",
      workPhone: "12",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot delete Alice's listing
  await assertFails(bobDb.collection("service_providers").doc("sp_1").delete());
  await assertSucceeds(aliceDb.collection("service_providers").doc("sp_1").delete());
});

// 5. Community posts: create OPEN, mark RESOLVED (terminal state lock), reject post-RESOLVED edits
test("Community posts: create OPEN, mark RESOLVED, and enforce terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 3000);

  await assertSucceeds(
    aliceDb.collection("community_posts").doc("post_1").set({
      userId: ALICE_UID,
      authorName: "أبو محمد",
      title: "مطلوب سلم ألمنيوم",
      description: "لإصلاح لمبة المدخل وسأعيده خلال ساعة.",
      category: "HELP",
      sector: "حي الندى",
      contactNote: "أمام البقالة",
      status: "OPEN",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Neighbor Bob can list via filtered query
  await assertSucceeds(
    bobDb
      .collection("community_posts")
      .where("visibility", "==", "NEIGHBORHOOD")
      .get()
  );

  // Bob cannot resolve Alice's post
  await assertFails(
    bobDb.collection("community_posts").doc("post_1").update({
      status: "RESOLVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Alice resolves her post (matches HayAlWurudRepository.markPostResolved)
  await assertSucceeds(
    aliceDb.collection("community_posts").doc("post_1").update({
      status: "RESOLVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state locking: once RESOLVED, further updates fail
  await assertFails(
    aliceDb.collection("community_posts").doc("post_1").update({
      status: "OPEN",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

// 6. Service recommendations: one-per-resident deterministic ID, existence check, no updates, owner-only delete
test("Service recommendations: deterministic endorsement ID & parent existence enforced", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("service_providers").doc("sp_trust").set({
      userId: ALICE_UID,
      authorName: "أبو محمد",
      name: "المعلم أبو أحمد",
      category: "MAINTENANCE",
      specialty: "كهربائي منازل",
      sector: "الساحة الرئيسية",
      workPhone: "0933112244",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob recommends Alice's craftsman using deterministic ID sp_trust_bob_resident_2
  const bobRecId = `sp_trust_${BOB_UID}`;
  await assertSucceeds(
    bobDb.collection("service_recommendations").doc(bobRecId).set({
      userId: BOB_UID,
      serviceId: "sp_trust",
      authorName: "جارك أبو علي",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
    })
  );

  // Filtered query succeeds
  await assertSucceeds(
    aliceDb
      .collection("service_recommendations")
      .where("visibility", "==", "NEIGHBORHOOD")
      .get()
  );

  // Arbitrary doc ID (attempting duplicate vote) fails
  await assertFails(
    bobDb.collection("service_recommendations").doc("arbitrary_vote_id").set({
      userId: BOB_UID,
      serviceId: "sp_trust",
      authorName: "جارك أبو علي",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
    })
  );

  // Orphaned recommendation for non-existent service fails
  await assertFails(
    bobDb.collection("service_recommendations").doc(`missing_svc_${BOB_UID}`).set({
      userId: BOB_UID,
      serviceId: "missing_svc",
      authorName: "جارك أبو علي",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
    })
  );

  // Updates are immutable (forbidden)
  await assertFails(
    bobDb.collection("service_recommendations").doc(bobRecId).update({
      authorName: "اسم معدل",
    })
  );

  // Alice cannot delete Bob's recommendation; Bob can
  await assertFails(aliceDb.collection("service_recommendations").doc(bobRecId).delete());
  await assertSucceeds(bobDb.collection("service_recommendations").doc(bobRecId).delete());
});

// 7. Stores & store products: shop owner CRUD, parent-store ownership gate, filtered neighborhood queries
test("Stores & store products: owner registration, parent gate, partial updates, and isolation enforced", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);

  // Unauthenticated reads/writes fail
  await assertFails(
    unauthDb.collection("stores").where("visibility", "==", "NEIGHBORHOOD").get()
  );
  await assertFails(
    unauthDb.collection("store_products").where("visibility", "==", "NEIGHBORHOOD").get()
  );

  // Alice registers her clothing & shoes shop
  await assertSucceeds(
    aliceDb.collection("stores").doc("store_1").set({
      userId: ALICE_UID,
      ownerName: "أبو سامر",
      name: "بوتيك الياسمين للألبسة والأحذية",
      category: "CLOTHING",
      sector: "الساحة الرئيسية",
      phone: "0933445566",
      workingHours: "10 صباحاً - 9 مساءً",
      description: "ألبسة نسائية ورجالية وأحذية موسمية",
      storeOpen: true,
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob can query stores using filtered neighborhood query, not unfiltered
  await assertSucceeds(
    bobDb.collection("stores").where("visibility", "==", "NEIGHBORHOOD").get()
  );
  await assertFails(bobDb.collection("stores").get());

  // Alice adds a product to her own store
  await assertSucceeds(
    aliceDb.collection("store_products").doc("prod_1").set({
      storeId: "store_1",
      userId: ALICE_UID,
      storeName: "بوتيك الياسمين للألبسة والأحذية",
      storeCategory: "CLOTHING",
      storeSector: "الساحة الرئيسية",
      storePhone: "0933445566",
      title: "جاكيت جينز شتوي",
      priceText: "185 ألف ل.س",
      originalPriceText: "230 ألف ل.س",
      onOffer: true,
      inStock: true,
      specsText: "المقاسات: M / L / XL",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Parent gate: Bob cannot add a product to Alice's store even with his own UID
  await assertFails(
    bobDb.collection("store_products").doc("prod_hijack").set({
      storeId: "store_1",
      userId: BOB_UID,
      storeName: "بوتيك الياسمين للألبسة والأحذية",
      storeCategory: "CLOTHING",
      storeSector: "الساحة الرئيسية",
      storePhone: "0933445566",
      title: "منتج مخترق",
      priceText: "10 آلاف",
      onOffer: false,
      inStock: true,
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Orphaned storeId fails
  await assertFails(
    aliceDb.collection("store_products").doc("prod_orphan").set({
      storeId: "nonexistent_store",
      userId: ALICE_UID,
      storeName: "محل وهمي",
      storeCategory: "SHOES",
      storeSector: "الساحة الرئيسية",
      storePhone: "0933445566",
      title: "حذاء رياضي",
      priceText: "140 ألف",
      onOffer: false,
      inStock: true,
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered product query succeeds for neighbor Bob; unfiltered fails
  await assertSucceeds(
    bobDb.collection("store_products").where("visibility", "==", "NEIGHBORHOOD").get()
  );
  await assertFails(bobDb.collection("store_products").get());

  // Alice toggles offer / stock status via partial update
  await assertSucceeds(
    aliceDb.collection("store_products").doc("prod_1").update({
      inStock: false,
      onOffer: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot update or delete Alice's store or product
  await assertFails(
    bobDb.collection("store_products").doc("prod_1").update({
      priceText: "مجاناً",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(bobDb.collection("stores").doc("store_1").delete());
  await assertSucceeds(aliceDb.collection("store_products").doc("prod_1").delete());
  await assertSucceeds(aliceDb.collection("stores").doc("store_1").delete());
});

// 8. Store reviews: one-per-resident deterministic ID, anti-self-rating, bounds (1..5), partial updates
test("Store reviews: deterministic review ID, anti-self-rating, bounds, and owner-only edit/delete enforced", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 3000);

  // Unauthenticated reads fail
  await assertFails(
    unauthDb.collection("store_reviews").where("visibility", "==", "NEIGHBORHOOD").get()
  );

  // Alice creates a store
  await assertSucceeds(
    aliceDb.collection("stores").doc("store_rate_1").set({
      userId: ALICE_UID,
      ownerName: "أبو سامر",
      name: "مكتبة الهدى",
      category: "STATIONERY",
      sector: "شارع المدارس",
      phone: "0933123456",
      storeOpen: true,
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Store owner Alice cannot rate her own store
  await assertFails(
    aliceDb.collection("store_reviews").doc(`store_rate_1_${ALICE_UID}`).set({
      storeId: "store_rate_1",
      userId: ALICE_UID,
      reviewerName: "أبو سامر",
      serviceQualityRating: 5,
      offerCredibilityRating: 5,
      overallRating: 5.0,
      comment: "أفضل متجر!",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Neighbor Bob submits a valid review with deterministic ID store_rate_1_bob_resident_2
  const bobReviewId = `store_rate_1_${BOB_UID}`;
  await assertSucceeds(
    bobDb.collection("store_reviews").doc(bobReviewId).set({
      storeId: "store_rate_1",
      userId: BOB_UID,
      reviewerName: "أبو علي",
      serviceQualityRating: 5,
      offerCredibilityRating: 4,
      overallRating: 4.5,
      comment: "تعامل ممتاز وأسعار العروض مطابقة تماماً للإعلان.",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered neighborhood query succeeds; unfiltered fails
  await assertSucceeds(
    aliceDb.collection("store_reviews").where("visibility", "==", "NEIGHBORHOOD").get()
  );
  await assertFails(aliceDb.collection("store_reviews").get());

  // Arbitrary doc ID (duplicate rating bypass) fails
  await assertFails(
    bobDb.collection("store_reviews").doc("arbitrary_review_id").set({
      storeId: "store_rate_1",
      userId: BOB_UID,
      reviewerName: "أبو علي",
      serviceQualityRating: 5,
      offerCredibilityRating: 5,
      overallRating: 5.0,
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Rating out of bounds (0 or 6) fails
  await assertFails(
    bobDb.collection("store_reviews").doc(bobReviewId).update({
      serviceQualityRating: 6,
      overallRating: 5.0,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Orphaned storeId fails
  await assertFails(
    bobDb.collection("store_reviews").doc(`missing_store_${BOB_UID}`).set({
      storeId: "missing_store",
      userId: BOB_UID,
      reviewerName: "أبو علي",
      serviceQualityRating: 4,
      offerCredibilityRating: 4,
      overallRating: 4.0,
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob updates his own review ratings and comment
  await assertSucceeds(
    bobDb.collection("store_reviews").doc(bobReviewId).update({
      serviceQualityRating: 5,
      offerCredibilityRating: 5,
      overallRating: 5.0,
      comment: "تم الشراء مرة ثانية، مصداقية عالية جداً.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Alice cannot edit or delete Bob's review
  await assertFails(
    aliceDb.collection("store_reviews").doc(bobReviewId).update({
      serviceQualityRating: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(aliceDb.collection("store_reviews").doc(bobReviewId).delete());

  // Bob can delete his own review
  await assertSucceeds(bobDb.collection("store_reviews").doc(bobReviewId).delete());
});

// 9. Delivery requests: create PENDING, filtered query, status transition lock, owner-only mutation/delete
test("Delivery requests: create PENDING, filtered query, status lock, and cross-user protection enforced", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 3000);

  // Unauthenticated reads fail
  await assertFails(
    unauthDb.collection("delivery_requests").where("visibility", "==", "NEIGHBORHOOD").get()
  );

  // Alice creates a delivery request with GPS URL
  await assertSucceeds(
    aliceDb.collection("delivery_requests").doc("del_1").set({
      userId: ALICE_UID,
      orderType: "PHARMACY",
      vehicleType: "MOTORCYCLE",
      pickupLocation: "صيدلية الورود المناوبة",
      orderDetails: "شراب سعال وفيتامين سي عدد 2",
      deliveryZone: "INSIDE_HAY_AL_WURUD",
      detailedAddress: "شارع المدارس، بناء 14، طابق 2",
      gpsMapsUrl: "https://maps.google.com/?q=33.532100,36.245100",
      customerName: "أبو محمد",
      customerPhone: "0933445566",
      status: "PENDING",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered query succeeds for neighbor Bob; unfiltered fails
  await assertSucceeds(
    bobDb.collection("delivery_requests").where("visibility", "==", "NEIGHBORHOOD").get()
  );
  await assertFails(bobDb.collection("delivery_requests").get());

  // Creating a request already COMPLETED fails
  await assertFails(
    aliceDb.collection("delivery_requests").doc("del_bad_status").set({
      userId: ALICE_UID,
      orderType: "GROCERY",
      vehicleType: "SEDAN",
      pickupLocation: "سمانة البركة",
      orderDetails: "أغراض منزلية",
      deliveryZone: "QUDSAYA_SUBURB",
      detailedAddress: "الجزيرة السابعة",
      customerName: "أبو محمد",
      customerPhone: "0933445566",
      status: "COMPLETED",
      visibility: "NEIGHBORHOOD",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot modify or delete Alice's delivery request
  await assertFails(
    bobDb.collection("delivery_requests").doc("del_1").update({
      status: "CANCELLED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("delivery_requests").doc("del_1").delete());

  // Alice marks her delivery request as COMPLETED
  await assertSucceeds(
    aliceDb.collection("delivery_requests").doc("del_1").update({
      status: "COMPLETED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: once COMPLETED, further updates fail
  await assertFails(
    aliceDb.collection("delivery_requests").doc("del_1").update({
      status: "CANCELLED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Alice can delete her delivery request
  await assertSucceeds(aliceDb.collection("delivery_requests").doc("del_1").delete());
});


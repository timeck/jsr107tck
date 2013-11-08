package org.jsr107.tck;

import domain.Beagle;
import domain.BorderCollie;
import domain.Chihuahua;
import domain.Dachshund;
import domain.Dog;
import domain.Hound;
import domain.Identifier;
import domain.Papillon;
import domain.RoughCoatedCollie;
import org.jsr107.tck.testutil.CacheTestSupport;
import org.junit.After;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;

import java.io.NotSerializableException;

import static domain.Sex.FEMALE;
import static domain.Sex.MALE;
import static javax.cache.expiry.Duration.ONE_HOUR;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests of type interactions with Caches
 *
 * @author Greg Luck
 */
public class TypesTest extends CacheTestSupport<Identifier, String> {

  private CacheManager cacheManager = getCacheManager();

  private Beagle pistachio = (Beagle) new Beagle().name(new Identifier("Pistachio")).color("tricolor").sex(MALE).weight(7);
  private RoughCoatedCollie juno = (RoughCoatedCollie) new RoughCoatedCollie().name(new Identifier("Juno")).sex(MALE).weight(7);
  private Dachshund skinny = (Dachshund) new Dachshund().name(new Identifier("Skinny")).sex(MALE).weight(5).neutered(true);
  private Chihuahua tonto = (Chihuahua) new Chihuahua().name(new Identifier("Tonto")).weight(3).sex(MALE).neutered(false);
  private BorderCollie bonzo = (BorderCollie) new BorderCollie().name(new Identifier("Bonzo")).color("tricolor").sex(FEMALE).weight(10);
  private Papillon talker = (Papillon) new Papillon().name(new Identifier("Talker")).color("Black and White").weight(4).sex(MALE);
  private final String cacheName = "sampleCache";

  protected MutableConfiguration<Identifier, String> newMutableConfiguration() {
    return new MutableConfiguration<Identifier, String>().setTypes(Identifier.class, String.class);
  }

  @After
  public void teardown() {
    cacheManager.close();
  }

  /**
   * What happens when you:
   *
   * 1) don't declare using generics and
   * 2) don't specify types during configuration.
   */
  @Test
  public void simpleAPINoGenericsAndNoTypeEnforcement() {

    MutableConfiguration config = new MutableConfiguration();
    Cache cache = cacheManager.createCache(cacheName, config);

    //can put different things in
    cache.put(1, "something");
    cache.put(pistachio.getName(), pistachio);
    cache.put(tonto.getName(), tonto);
    cache.put(bonzo.getName(), bonzo);
    cache.put(juno.getName(), juno);
    cache.put(talker.getName(), talker);

    try {
      cache.put(skinny.getName(), skinny);
    } catch(Exception e) {
      //not serializable expected
    }
    //can get them out
    assertNotNull(cache.get(1));
    assertNotNull(cache.get(pistachio.getName()));

    //can remove them
    assertTrue(cache.remove(1));
    assertTrue(cache.remove(pistachio.getName()));
  }

  /**
   * What happens when you:
   *
   * 1) declare using generics and
   * 2) don't specify types during configuration.
   */
  @Test
  public void simpleAPIWithGenericsAndNoTypeEnforcement() {

    MutableConfiguration config = new MutableConfiguration<String, Integer>();
    Cache<Identifier, Dog> cache = cacheManager.createCache(cacheName, config);


    //Types are restricted
    //Cannot put in wrong types
    //cache.put(1, "something");

    //can put in
    cache.put(pistachio.getName(), pistachio);
    cache.put(tonto.getName(), tonto);

    //cannot get out wrong key types
    //assertNotNull(cache.get(1));
    assertNotNull(cache.get(pistachio.getName()));
    assertNotNull(cache.get(tonto.getName()));

    //cannot remove wrong key types
    //assertTrue(cache.remove(1));
    assertTrue(cache.remove(pistachio.getName()));
    assertTrue(cache.remove(tonto.getName()));

  }


  /**
   * What happens when you:
   *
   * 1) declare using generics with a super class
   * 2) declare using configuration with a sub class
   *
   * Set generics to Identifier and Dog
   * Bypass generics with a raw MutableConfiguration but set runtime to Identifier and Hound
   *
   * The configuration checking gets done on put.
   */
  @Test
  public void genericsEnforcementAndStricterTypeEnforcement() {

    //configure the cache
    MutableConfiguration config = new MutableConfiguration<>();
    config.setTypes(Identifier.class, Hound.class);
    Cache<Identifier, Dog> cache = cacheManager.createCache(cacheName, config);

    //Types are restricted and types are enforced
    //Cannot put in wrong types
    //cache.put(1, "something");

    //can put in
    cache.put(pistachio.getName(), pistachio);
    //can put in with generics but possibly not with configuration as not a hound
    try {
      cache.put(tonto.getName(), tonto);
    } catch (ClassCastException e) {
      //expected but not mandatory. The RI throws these.
    }

    //cannot get out wrong key types
    //assertNotNull(cache.get(1));
    assertNotNull(cache.get(pistachio.getName()));
    //not necessarily
    //assertNotNull(cache.get(tonto.getName()));

    //cannot remove wrong key types
    //assertTrue(cache.remove(1));
    assertTrue(cache.remove(pistachio.getName()));
    //not necessarily
    //assertTrue(cache.remove(tonto.getName()));
  }


  /**
   * Same as above but using the shorthand Caching to acquire.
   * Should work the same.
   */
  @Test
  public void genericsEnforcementAndStricterTypeEnforcementFromCaching() {

    //configure the cache
    MutableConfiguration config = new MutableConfiguration<>();
    config.setTypes(Identifier.class, Hound.class);
    Cache<Identifier, Dog> cache = cacheManager.createCache(cacheName, config);

    //Types are restricted and types are enforced
    //Cannot put in wrong types
    //cache.put(1, "something");

    //can put in
    cache.put(pistachio.getName(), pistachio);
    //can put in with generics but possibly not with configuration as not a hound
    try {
      cache.put(tonto.getName(), tonto);
    } catch (ClassCastException e) {
      //expected but not mandatory. The RI throws these.
    }

    //cannot get out wrong key types
    //assertNotNull(cache.get(1));
    assertNotNull(cache.get(pistachio.getName()));
    //not necessarily
    //assertNotNull(cache.get(tonto.getName()));

    //cannot remove wrong key types
    //assertTrue(cache.remove(1));
    assertTrue(cache.remove(pistachio.getName()));
    //not necessarily
    //assertTrue(cache.remove(tonto.getName()));
  }

  /**
   * What happens when you:
   *
   * 1) declare using generics and
   * 2) specify types during configuration but using Object.class, which is permissive
   */
  @Test
  public void simpleAPITypeEnforcementObject() {


    //configure the cache
    MutableConfiguration<Object, Object> config = new MutableConfiguration<>();
    config.setTypes(Object.class, Object.class);

    //create the cache
    Cache<Object, Object> cache = cacheManager.createCache("simpleCache4", config);

    //can put different things in
    cache.put(1, "something");
    cache.put(pistachio.getName(), pistachio);
    cache.put(tonto.getName(), tonto);
    cache.put(bonzo.getName(), bonzo);
    cache.put(juno.getName(), juno);
    cache.put(talker.getName(), talker);
    try {
      cache.put(skinny.getName(), skinny);
    } catch(Exception e) {
      //not serializable expected
    }
    //can get them out
    assertNotNull(cache.get(1));
    assertNotNull(cache.get(pistachio.getName()));

    //can remove them
    assertTrue(cache.remove(1));
    assertTrue(cache.remove(pistachio.getName()));
  }








}

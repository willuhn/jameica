/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.ssh;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

/**
 * Tests der interaktiven Hostkey-Pruefung.
 * 
 * <p>Der Benutzer-Dialog wird durch eine Test-Unterklasse ersetzt, sodass
 * die Entscheidungslogik ohne GUI und ohne laufende Jameica-Instanz geprueft
 * werden kann.
 */
public class PromptingHostKeyRepositoryTest
{
  private static final String HOST = "ssh.example.org";

  /**
   * Test-Repository, das die Benutzer-Frage nicht anzeigt, sondern eine
   * vorgegebene Antwort liefert und die Aufrufe zaehlt.
   */
  private static class TestRepository extends PromptingHostKeyRepository
  {
    boolean unknownAnswer = false;
    boolean changedAnswer = false;
    int unknownCalls      = 0;
    int changedCalls      = 0;

    TestRepository(JSch jsch, HostKeyRepository delegate)
    {
      super(jsch,delegate);
    }

    boolean askUnknown(String host, HostKey key)
    {
      this.unknownCalls++;
      return this.unknownAnswer;
    }

    boolean askChanged(String host, HostKey key)
    {
      this.changedCalls++;
      return this.changedAnswer;
    }
  }

  /**
   * Liefert einen frischen ED25519-Public-Key-Blob.
   * @param jsch die JSch-Instanz.
   * @return der Public-Key-Blob.
   * @throws Exception wenn das Schluesselpaar nicht erzeugt werden konnte.
   */
  private byte[] newKeyBlob(JSch jsch) throws Exception
  {
    KeyPair kp = KeyPair.genKeyPair(jsch,KeyPair.ED25519);
    try
    {
      return kp.getPublicKeyBlob();
    }
    finally
    {
      kp.dispose();
    }
  }

  /**
   * Schreibt eine known_hosts-Datei mit dem angegebenen Eintrag.
   * @param file die Datei.
   * @param host der Hostname.
   * @param blob der Public-Key-Blob.
   * @return der Dateiinhalt.
   * @throws Exception wenn die Datei nicht geschrieben werden konnte.
   */
  private Path writeKnownHosts(Path file, String host, byte[] blob) throws Exception
  {
    String line = host + " ssh-ed25519 " + Base64.getEncoder().encodeToString(blob) + "\n";
    Files.write(file,line.getBytes(StandardCharsets.US_ASCII));
    return file;
  }

  /**
   * Ein bereits bekannter und passender Hostkey wird ohne Rueckfrage akzeptiert.
   */
  @Test
  public void knownKeyIsAcceptedWithoutPrompt() throws Exception
  {
    JSch jsch = new JSch();
    byte[] key = newKeyBlob(jsch);

    Path file = writeKnownHosts(Files.createTempFile("known_hosts",".txt"),HOST,key);
    jsch.setKnownHosts(file.toString());

    TestRepository repo = new TestRepository(jsch,jsch.getHostKeyRepository());
    Assert.assertEquals(HostKeyRepository.OK,repo.check(HOST,key));
    Assert.assertEquals(0,repo.unknownCalls);
    Assert.assertEquals(0,repo.changedCalls);
  }

  /**
   * Ein unbekannter Hostkey wird bei Zustimmung eingetragen.
   */
  @Test
  public void unknownKeyIsAddedOnConsent() throws Exception
  {
    JSch jsch = new JSch();
    byte[] key = newKeyBlob(jsch);

    Path file = writeKnownHosts(Files.createTempFile("known_hosts",".txt"),"other.example.org",newKeyBlob(jsch));
    jsch.setKnownHosts(file.toString());

    TestRepository repo = new TestRepository(jsch,jsch.getHostKeyRepository());
    repo.unknownAnswer = true;

    Assert.assertEquals(HostKeyRepository.OK,repo.check(HOST,key));
    Assert.assertEquals(1,repo.unknownCalls);
    Assert.assertTrue(Files.readString(file).contains(Base64.getEncoder().encodeToString(key)));
  }

  /**
   * Ein unbekannter Hostkey wird bei Ablehnung nicht eingetragen.
   */
  @Test
  public void unknownKeyIsRejectedOnRefusal() throws Exception
  {
    JSch jsch = new JSch();
    byte[] key = newKeyBlob(jsch);

    Path file = writeKnownHosts(Files.createTempFile("known_hosts",".txt"),"other.example.org",newKeyBlob(jsch));
    String before = Files.readString(file);
    jsch.setKnownHosts(file.toString());

    TestRepository repo = new TestRepository(jsch,jsch.getHostKeyRepository());
    repo.unknownAnswer = false;

    Assert.assertEquals(HostKeyRepository.NOT_INCLUDED,repo.check(HOST,key));
    Assert.assertEquals(1,repo.unknownCalls);
    Assert.assertEquals(before,Files.readString(file));
  }

  /**
   * Ein geaenderter Hostkey ersetzt bei Zustimmung den alten Eintrag.
   */
  @Test
  public void changedKeyIsReplacedOnConsent() throws Exception
  {
    JSch jsch = new JSch();
    byte[] oldKey = newKeyBlob(jsch);
    byte[] newKey = newKeyBlob(jsch);

    Path file = writeKnownHosts(Files.createTempFile("known_hosts",".txt"),HOST,oldKey);
    jsch.setKnownHosts(file.toString());

    TestRepository repo = new TestRepository(jsch,jsch.getHostKeyRepository());
    Assert.assertEquals(HostKeyRepository.CHANGED,repo.check(HOST,newKey));

    repo.changedAnswer = true;
    Assert.assertEquals(HostKeyRepository.OK,repo.check(HOST,newKey));
    Assert.assertEquals(2,repo.changedCalls);

    String content = Files.readString(file);
    Assert.assertTrue(content.contains(Base64.getEncoder().encodeToString(newKey)));
    Assert.assertFalse(content.contains(Base64.getEncoder().encodeToString(oldKey)));

    // Nach dem Ersetzen liefert eine frische Pruefung direkt OK (keine weitere Frage).
    Assert.assertEquals(HostKeyRepository.OK,repo.check(HOST,newKey));
    Assert.assertEquals(2,repo.changedCalls);
  }

  /**
   * Ein geaenderter Hostkey bleibt bei Ablehnung unveraendert und wird
   * weiterhin als geaendert gemeldet.
   */
  @Test
  public void changedKeyIsKeptOnRefusal() throws Exception
  {
    JSch jsch = new JSch();
    byte[] oldKey = newKeyBlob(jsch);
    byte[] newKey = newKeyBlob(jsch);

    Path file = writeKnownHosts(Files.createTempFile("known_hosts",".txt"),HOST,oldKey);
    String before = Files.readString(file);
    jsch.setKnownHosts(file.toString());

    TestRepository repo = new TestRepository(jsch,jsch.getHostKeyRepository());
    repo.changedAnswer = false;

    Assert.assertEquals(HostKeyRepository.CHANGED,repo.check(HOST,newKey));
    Assert.assertEquals(1,repo.changedCalls);
    Assert.assertEquals(before,Files.readString(file));
  }

  /**
   * Ein unbekannter Hostkey wird auch bei noch nicht existierender
   * known_hosts-Datei angelegt.
   */
  @Test
  public void missingKnownHostsFileIsCreatedOnConsent() throws Exception
  {
    JSch jsch = new JSch();
    byte[] key = newKeyBlob(jsch);

    Path file = Files.createTempDirectory("khtest").resolve("known_hosts");
    Assert.assertFalse(Files.exists(file));
    jsch.setKnownHosts(file.toString());

    TestRepository repo = new TestRepository(jsch,jsch.getHostKeyRepository());
    repo.unknownAnswer = true;

    Assert.assertEquals(HostKeyRepository.OK,repo.check(HOST,key));
    Assert.assertTrue(Files.exists(file));
    Assert.assertTrue(Files.readString(file).contains(Base64.getEncoder().encodeToString(key)));
  }
}

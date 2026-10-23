/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.update;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.junit.Assert;
import org.junit.Test;

import de.willuhn.security.Signature;
import de.willuhn.util.ApplicationException;

/**
 * Tests unattended plugin signature policy.
 */
public class RepositorySignaturePolicyTest
{
  /** Signed automatic downloads require a verification certificate. */
  @Test
  public void acceptAuthenticatedAutomaticDownload() throws Exception
  {
    Repository.enforceSignaturePolicy(false,true,true);
  }

  /** Missing signatures fail closed without a user trust decision. */
  @Test(expected=ApplicationException.class)
  public void rejectUnsignedAutomaticDownload() throws Exception
  {
    Repository.enforceSignaturePolicy(false,false,true);
  }

  /** A signature without its verification certificate is not authenticated. */
  @Test(expected=ApplicationException.class)
  public void rejectAutomaticDownloadWithoutCertificate() throws Exception
  {
    Repository.enforceSignaturePolicy(false,true,false);
  }

  /** Existing interactive unsigned installation remains an explicit choice. */
  @Test
  public void allowInteractiveUnsignedDecision() throws Exception
  {
    Repository.enforceSignaturePolicy(true,false,false);
  }

  /** The existing verification primitive rejects a signature over different bytes. */
  @Test
  public void rejectIncorrectSignature() throws Exception
  {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(1024);
    KeyPair pair = generator.generateKeyPair();
    byte[] original = "original plugin archive".getBytes(StandardCharsets.UTF_8);
    byte[] changed = "changed plugin archive".getBytes(StandardCharsets.UTF_8);
    byte[] signature = Signature.sign(new ByteArrayInputStream(original),pair.getPrivate());
    Assert.assertFalse(Signature.verifiy(new ByteArrayInputStream(changed),pair.getPublic(),signature));
  }
}

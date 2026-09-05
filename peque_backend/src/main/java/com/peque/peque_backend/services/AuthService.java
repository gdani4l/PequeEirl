package com.peque.peque_backend.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.peque.peque_backend.dtos.LoginRequestDTO;
import com.peque.peque_backend.dtos.LoginResponseDTO;
import com.peque.peque_backend.dtos.RegistroRequestDTO;
import com.peque.peque_backend.models.Direcciones;
import com.peque.peque_backend.models.EmailVerificationToken;
import com.peque.peque_backend.models.MfaCodigo;
import com.peque.peque_backend.models.Rol;
import com.peque.peque_backend.models.Telefonos;
import com.peque.peque_backend.models.TipoDocumento;
import com.peque.peque_backend.models.Usuario;
import com.peque.peque_backend.models.Vendedor;
import com.peque.peque_backend.repositories.DireccionesRepository;
import com.peque.peque_backend.repositories.EmailVerificationTokenRepository;
import com.peque.peque_backend.repositories.MfaCodigoRepository;
import com.peque.peque_backend.repositories.RolRepository;
import com.peque.peque_backend.repositories.TelefonosRepository;
import com.peque.peque_backend.repositories.TipoDocumentoRepository;
import com.peque.peque_backend.repositories.UsuarioRepository;
import com.peque.peque_backend.repositories.VendedorRepository;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TelefonosRepository telefonosRepository;

    @Autowired
    private DireccionesRepository direccionesRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MfaCodigoRepository mfaCodigoRepository;

    @Autowired
    private com.peque.peque_backend.security.JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final BytesKeyGenerator TOKEN_GENERATOR = KeyGenerators.secureRandom(15);

    private String generateToken() {
        byte[] tokenBytes = TOKEN_GENERATOR.generateKey();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    @Transactional
    public String registrar(RegistroRequestDTO request) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByCorreo(request.getCorreo());
        if (usuarioExistente.isPresent()) {
            return "El correo ya está registrado";
        }

        TipoDocumento tipoDoc = tipoDocumentoRepository.findByAbreviatura(request.getTipoDocumento())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));

        Rol rolVendedor = rolRepository.findByNombre("VENDEDOR")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setTipoDocumento(tipoDoc);
        usuario.setRol(rolVendedor);
        usuario.setNombre(request.getNombre());
        usuario.setSegundo_nombre(request.getSegundoNombre());
        usuario.setApellido_pat(request.getApellidoPat());
        usuario.setApellido_mat(request.getApellidoMat());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setFecha_registro(LocalDate.now());
        usuario.setActivo(true);
        usuario.setEmail_verified(false);
        usuario.setNumero_documento(request.getNumeroDocumento());
        usuario.setCodigo_postal(request.getCodigo_postal());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Telefonos telefono = new Telefonos();
        telefono.setNumero(request.getTelefono());
        telefono.setUsuario(usuarioGuardado);
        telefonosRepository.save(telefono);

        Direcciones direccion = new Direcciones();
        direccion.setDepartamento(request.getDepartamento());
        direccion.setProvincia(request.getProvincia());
        direccion.setDistrito(request.getDistrito());
        direccion.setCalle(request.getCalle());
        direccion.setReferencia(request.getReferencia() != null ? request.getReferencia() : "");
        direccion.setUsuario(usuarioGuardado);
        direccionesRepository.save(direccion);

        Vendedor vendedor = new Vendedor(usuarioGuardado.getId_usuario());
        vendedorRepository.save(vendedor);

        String tokenValue = generateToken();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(tokenValue);
        verificationToken.setUsuario(usuarioGuardado);
        verificationToken.setExpires_at(LocalDateTime.now().plusHours(24));
        verificationToken.setIs_used(false);
        verificationToken.setCreated_at(LocalDateTime.now());
        emailVerificationTokenRepository.save(verificationToken);

        final String correoDestino = usuarioGuardado.getCorreo();
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendVerificationEmail(correoDestino, tokenValue);
            } catch (Exception ex) {
                System.err.println("Error al enviar el correo de verificacion: " + ex.getMessage());
            }
        });

        return "Registro exitoso. Por favor, revisa tu correo para activar tu cuenta.";
    }

    @Transactional
    public String verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token de verificación inválido."));

        if (verificationToken.getIs_used()) {
            throw new RuntimeException("Este token de verificación ya ha sido utilizado.");
        }

        if (verificationToken.getExpires_at().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token de verificación ha expirado.");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setEmail_verified(true);
        usuario.setEmail_verified_at(LocalDateTime.now());
        usuarioRepository.save(usuario);

        verificationToken.setIs_used(true);
        emailVerificationTokenRepository.save(verificationToken);

        return "Cuenta verificada exitosamente. Ya puedes iniciar sesión.";
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo()).orElse(null);

        if (usuario == null) {
            return new LoginResponseDTO(null, null, null, null, null, null, "Usuario no encontrado");
        }

        if (!usuario.getActivo()) {
            return new LoginResponseDTO(null, null, null, null, null, null,
                    "Cuenta desactivada. Contacte al administrador.");
        }

        if (!usuario.getEmail_verified()) {
            return new LoginResponseDTO(null, null, null, null, null, null,
                    "Por favor, verifica tu cuenta de correo electrónico antes de iniciar sesión.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return new LoginResponseDTO(null, null, null, null, null, null, "Contraseña incorrecta");
        }

        Optional<MfaCodigo> verificadoReciente = mfaCodigoRepository
                .buscarVerificadoReciente(usuario.getId_usuario(), LocalDateTime.now().minusMinutes(10));
        if (verificadoReciente.isPresent()) {
            LoginResponseDTO directo = new LoginResponseDTO(
                    usuario.getId_usuario(),
                    usuario.getNombre(),
                    usuario.getApellido_pat(),
                    usuario.getApellido_mat(),
                    usuario.getCorreo(),
                    usuario.getRol().getNombre(),
                    "Login exitoso");
            directo.setToken(jwtUtil.generarToken(
                    usuario.getId_usuario(), usuario.getCorreo(), usuario.getRol().getNombre()));
            return directo;
        }

        String codigo = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));

        Optional<MfaCodigo> vigente = mfaCodigoRepository.buscarVigentePorUsuario(usuario.getId_usuario());
        if (vigente.isPresent()) {
            MfaCodigo anterior = vigente.get();
            anterior.setUsado(true);
            anterior.setIntentos(3);
            mfaCodigoRepository.save(anterior);
        }

        MfaCodigo mfa = new MfaCodigo();
        mfa.setId_usuario(usuario.getId_usuario());
        mfa.setCodigo(codigo);
        mfa.setExpira(LocalDateTime.now().plusMinutes(5));
        mfa.setUsado(false);
        mfa.setIntentos(0);
        mfa.setFecha_creacion(LocalDateTime.now());
        mfaCodigoRepository.save(mfa);

        emailService.sendMfaCode(usuario.getCorreo(), codigo);

        return new LoginResponseDTO(null, null, null, null, usuario.getCorreo(), null, "MFA_REQUERIDO");
    }

    public LoginResponseDTO verificarMfa(String correo, String codigo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            return new LoginResponseDTO(null, null, null, null, null, null, "Usuario no encontrado");
        }

        MfaCodigo mfa = mfaCodigoRepository.buscarVigentePorUsuario(usuario.getId_usuario()).orElse(null);
        if (mfa == null) {
            return new LoginResponseDTO(null, null, null, null, null, null,
                    "No hay un código vigente. Inicie sesión nuevamente.");
        }

        if (mfa.getExpira().isBefore(LocalDateTime.now())) {
            mfa.setUsado(true);
            mfa.setIntentos(3);
            mfaCodigoRepository.save(mfa);
            return new LoginResponseDTO(null, null, null, null, null, null,
                    "El código expiró. Inicie sesión nuevamente.");
        }

        if (mfa.getIntentos() >= 3) {
            mfa.setUsado(true);
            mfaCodigoRepository.save(mfa);
            return new LoginResponseDTO(null, null, null, null, null, null,
                    "Demasiados intentos. Inicie sesión nuevamente.");
        }

        if (!mfa.getCodigo().equals(codigo)) {
            mfa.setIntentos(mfa.getIntentos() + 1);
            mfaCodigoRepository.save(mfa);
            return new LoginResponseDTO(null, null, null, null, null, null, "Código incorrecto");
        }

        mfa.setUsado(true);
        mfaCodigoRepository.save(mfa);

        LoginResponseDTO response = new LoginResponseDTO(
                usuario.getId_usuario(),
                usuario.getNombre(),
                usuario.getApellido_pat(),
                usuario.getApellido_mat(),
                usuario.getCorreo(),
                usuario.getRol().getNombre(),
                "Login exitoso");
        response.setToken(jwtUtil.generarToken(
                usuario.getId_usuario(), usuario.getCorreo(), usuario.getRol().getNombre()));
        return response;
    }
}
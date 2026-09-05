package com.peque.peque_backend.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.peque.peque_backend.dtos.UsuarioResponseDTO;
import com.peque.peque_backend.dtos.UsuarioUpdateDTO;
import com.peque.peque_backend.models.Direcciones;
import com.peque.peque_backend.models.Rol;
import com.peque.peque_backend.models.Telefonos;
import com.peque.peque_backend.models.Usuario;
import com.peque.peque_backend.repositories.DireccionesRepository;
import com.peque.peque_backend.repositories.RolRepository;
import com.peque.peque_backend.repositories.TelefonosRepository;
import com.peque.peque_backend.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TelefonosRepository telefonosRepository;

    @Autowired
    private DireccionesRepository direccionesRepository;

    public List<UsuarioResponseDTO> getUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO getUsuarioById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertToDTO(usuario);
    }

    private UsuarioResponseDTO convertToDTO(Usuario usuario) {
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId_usuario(usuario.getId_usuario());
        response.setId_tipo_documento(usuario.getTipoDocumento().getId_tipo_documento());
        response.setTipo_documento_abreviatura(usuario.getTipoDocumento().getAbreviatura());
        response.setId_rol(usuario.getRol().getId_rol());
        response.setRol_nombre(usuario.getRol().getNombre());
        response.setNombre(usuario.getNombre());
        response.setSegundo_nombre(usuario.getSegundo_nombre());
        response.setApellido_pat(usuario.getApellido_pat());
        response.setApellido_mat(usuario.getApellido_mat());
        response.setCorreo(usuario.getCorreo());
        response.setActivo(usuario.getActivo());
        response.setNumero_documento(usuario.getNumero_documento());
        response.setCodigo_postal(usuario.getCodigo_postal());

        Optional<Telefonos> telefonoOpt = telefonosRepository.findByUsuarioId(usuario.getId_usuario());
        response.setTelefono(telefonoOpt.isPresent() ? telefonoOpt.get().getNumero() : null);

        Optional<Direcciones> direccionOpt = direccionesRepository.findByUsuarioId(usuario.getId_usuario());
        if (direccionOpt.isPresent()) {
            Direcciones direccion = direccionOpt.get();
            response.setDepartamento(direccion.getDepartamento());
            response.setProvincia(direccion.getProvincia());
            response.setDistrito(direccion.getDistrito());
            response.setCalle(direccion.getCalle());
            response.setReferencia(direccion.getReferencia());
        }

        return response;
    }

    @Transactional
    public String actualizarUsuario(Integer id, UsuarioUpdateDTO updateDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (updateDTO.getNombre() != null) {
            usuario.setNombre(updateDTO.getNombre());
        }
        if (updateDTO.getSegundo_nombre() != null) {
            usuario.setSegundo_nombre(updateDTO.getSegundo_nombre());
        }
        if (updateDTO.getApellido_pat() != null) {
            usuario.setApellido_pat(updateDTO.getApellido_pat());
        }
        if (updateDTO.getApellido_mat() != null) {
            usuario.setApellido_mat(updateDTO.getApellido_mat());
        }
        if (updateDTO.getCorreo() != null) {
            usuario.setCorreo(updateDTO.getCorreo());
        }
        if (updateDTO.getActivo() != null) {
            usuario.setActivo(updateDTO.getActivo());
        }
        if (updateDTO.getNumero_documento() != null) {
            usuario.setNumero_documento(updateDTO.getNumero_documento());
        }
        if (updateDTO.getCodigo_postal() != null) {
            usuario.setCodigo_postal(updateDTO.getCodigo_postal());
        }

        if (updateDTO.getRol() != null && !updateDTO.getRol().isEmpty()) {
            Optional<Rol> rolOpt = rolRepository.findByNombre(updateDTO.getRol());
            if (rolOpt.isPresent()) {
                usuario.setRol(rolOpt.get());
            } else {
                throw new RuntimeException("Rol no encontrado: " + updateDTO.getRol());
            }
        }

        usuarioRepository.save(usuario);

        if (updateDTO.getTelefono() != null && !updateDTO.getTelefono().isEmpty()) {
            Optional<Telefonos> telefonoOpt = telefonosRepository.findByUsuarioId(id);
            if (telefonoOpt.isPresent()) {
                Telefonos telefono = telefonoOpt.get();
                telefono.setNumero(updateDTO.getTelefono());
                telefonosRepository.save(telefono);
            } else {
                Telefonos nuevoTelefono = new Telefonos();
                nuevoTelefono.setNumero(updateDTO.getTelefono());
                nuevoTelefono.setUsuario(usuario);
                telefonosRepository.save(nuevoTelefono);
            }
        }

        Optional<Direcciones> direccionOpt = direccionesRepository.findByUsuarioId(id);
        if (direccionOpt.isPresent()) {
            Direcciones direccion = direccionOpt.get();
            if (updateDTO.getDepartamento() != null) {
                direccion.setDepartamento(updateDTO.getDepartamento());
            }
            if (updateDTO.getProvincia() != null) {
                direccion.setProvincia(updateDTO.getProvincia());
            }
            if (updateDTO.getDistrito() != null) {
                direccion.setDistrito(updateDTO.getDistrito());
            }
            if (updateDTO.getCalle() != null) {
                direccion.setCalle(updateDTO.getCalle());
            }
            if (updateDTO.getReferencia() != null) {
                direccion.setReferencia(updateDTO.getReferencia());
            }
            direccionesRepository.save(direccion);
        } else {
            Direcciones nuevaDireccion = new Direcciones();
            nuevaDireccion.setDepartamento(updateDTO.getDepartamento() != null ? updateDTO.getDepartamento() : "");
            nuevaDireccion.setProvincia(updateDTO.getProvincia() != null ? updateDTO.getProvincia() : "");
            nuevaDireccion.setDistrito(updateDTO.getDistrito() != null ? updateDTO.getDistrito() : "");
            nuevaDireccion.setCalle(updateDTO.getCalle() != null ? updateDTO.getCalle() : "");
            nuevaDireccion.setReferencia(updateDTO.getReferencia() != null ? updateDTO.getReferencia() : "");
            nuevaDireccion.setUsuario(usuario);
            direccionesRepository.save(nuevaDireccion);
        }

        return "Usuario actualizado correctamente";
    }
}
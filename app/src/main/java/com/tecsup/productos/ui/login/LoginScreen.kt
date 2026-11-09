package com.tecsup.productos.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.domain.validacion.ErrorCampo
import com.tecsup.productos.domain.validacion.ValidadorLogin
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.LogoStockify
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ProductosTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    formulario: FormularioLogin,
    estado: UiState<Sesion>?,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cargando = estado is UiState.Cargando
    val errorCorreo = formulario.errorCorreo
    val errorClave = formulario.errorClave
    val focusManager = LocalFocusManager.current
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val interaccionIngresar = remember { MutableInteractionSource() }
    val ingresar = {
        focusManager.clearFocus()
        onIngresar()
    }

    Surface(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) {
            val alturaMinima = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = alturaMinima)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LogoAnimado()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.login_bienvenida),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.login_subtitulo),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = formulario.correo,
                        onValueChange = onCorreoChange,
                        label = { Text(text = stringResource(R.string.login_correo)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_correo),
                                contentDescription = null
                            )
                        },
                        isError = errorCorreo != null,
                        supportingText = if (errorCorreo != null) {
                            { Text(text = mensajeErrorCorreo(errorCorreo)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        enabled = !cargando,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = formulario.clave,
                        onValueChange = onClaveChange,
                        label = { Text(text = stringResource(R.string.login_clave)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_candado),
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { mostrarClave = !mostrarClave }) {
                                Icon(
                                    painter = painterResource(
                                        if (mostrarClave) R.drawable.ic_ocultar else R.drawable.ic_mostrar
                                    ),
                                    contentDescription = stringResource(
                                        if (mostrarClave) {
                                            R.string.login_ocultar_clave
                                        } else {
                                            R.string.login_mostrar_clave
                                        }
                                    )
                                )
                            }
                        },
                        visualTransformation = if (mostrarClave) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = errorClave != null,
                        supportingText = if (errorClave != null) {
                            { Text(text = mensajeErrorClave(errorClave)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        enabled = !cargando,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { ingresar() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = estado is UiState.Error) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = (estado as? UiState.Error)?.mensaje.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = ingresar,
                        enabled = !cargando,
                        interactionSource = interaccionIngresar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .rebote(interaccionIngresar)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(text = stringResource(R.string.login_ingresar))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoAnimado() {
    val escala = remember { Animatable(ESCALA_INICIAL_LOGO) }
    val opacidad = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            escala.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        opacidad.animateTo(1f, animationSpec = tween(DURACION_APARICION_MS))
    }
    LogoStockify(
        tamano = 120.dp,
        modifier = Modifier.graphicsLayer {
            scaleX = escala.value
            scaleY = escala.value
            alpha = opacidad.value
        }
    )
}

private const val ESCALA_INICIAL_LOGO = 0.6f
private const val DURACION_APARICION_MS = 500

@Composable
private fun mensajeErrorCorreo(error: ErrorCampo): String = when (error) {
    ErrorCampo.VACIO -> stringResource(R.string.login_error_correo_vacio)
    else -> stringResource(R.string.login_error_correo_formato)
}

@Composable
private fun mensajeErrorClave(error: ErrorCampo): String = when (error) {
    ErrorCampo.VACIO -> stringResource(R.string.login_error_clave_vacia)
    else -> stringResource(R.string.login_error_clave_corta, ValidadorLogin.LONGITUD_MINIMA_CLAVE)
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    ProductosTheme {
        LoginScreen(
            formulario = FormularioLogin(),
            estado = null,
            onCorreoChange = {},
            onClaveChange = {},
            onIngresar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErroresPreview() {
    ProductosTheme {
        LoginScreen(
            formulario = FormularioLogin(
                correo = "admin@",
                errorCorreo = ErrorCampo.FORMATO_INVALIDO,
                errorClave = ErrorCampo.VACIO
            ),
            estado = null,
            onCorreoChange = {},
            onClaveChange = {},
            onIngresar = {}
        )
    }
}

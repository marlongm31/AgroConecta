import { Eye, EyeOff } from 'lucide-react';
import { useState } from 'react';
import type {
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from 'react';

interface FieldWrapperProps {
  label: string;
  htmlFor: string;
  error?: string;
  hint?: string;
  children: ReactNode;
}

function FieldWrapper({ label, htmlFor, error, hint, children }: FieldWrapperProps) {
  return (
    <div>
      <label className="label-base" htmlFor={htmlFor}>
        {label}
      </label>
      {children}
      {error ? (
        <p className="mt-1 text-xs font-medium text-red-600">{error}</p>
      ) : hint ? (
        <p className="mt-1 text-xs text-slate-500">{hint}</p>
      ) : null}
    </div>
  );
}

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
  endAdornment?: ReactNode;
}

export function TextField({ label, error, hint, id, className = '', endAdornment, ...rest }: TextFieldProps) {
  const fieldId = id ?? rest.name ?? label;
  return (
    <FieldWrapper label={label} htmlFor={fieldId} error={error} hint={hint}>
      <div className="relative">
        <input
          id={fieldId}
          className={`input-base ${error ? 'border-red-400 focus:border-red-500 focus:ring-red-200' : ''} ${endAdornment ? 'pr-11' : ''} ${className}`}
          aria-invalid={Boolean(error)}
          {...rest}
        />
        {endAdornment ? (
          <div className="absolute inset-y-0 right-0 flex items-center pr-2">{endAdornment}</div>
        ) : null}
      </div>
    </FieldWrapper>
  );
}

export function PasswordField({ label, ...rest }: TextFieldProps) {
  const [visible, setVisible] = useState(false);
  const Icon = visible ? EyeOff : Eye;

  return (
    <TextField
      label={label}
      {...rest}
      type={visible ? 'text' : 'password'}
      endAdornment={
        <button
          type="button"
          aria-label={`${visible ? 'Ocultar' : 'Mostrar'} ${label}`}
          onClick={() => setVisible((current) => !current)}
          className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100"
        >
          <Icon className="h-4 w-4" />
        </button>
      }
    />
  );
}

interface TextAreaFieldProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  error?: string;
}

export function TextAreaField({ label, error, id, className = '', ...rest }: TextAreaFieldProps) {
  const fieldId = id ?? rest.name ?? label;
  return (
    <FieldWrapper label={label} htmlFor={fieldId} error={error}>
      <textarea
        id={fieldId}
        rows={3}
        className={`input-base resize-y ${error ? 'border-red-400' : ''} ${className}`}
        aria-invalid={Boolean(error)}
        {...rest}
      />
    </FieldWrapper>
  );
}

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  error?: string;
  options: { value: string; label: string }[];
  placeholder?: string;
}

export function SelectField({
  label,
  error,
  options,
  placeholder,
  id,
  className = '',
  ...rest
}: SelectFieldProps) {
  const fieldId = id ?? rest.name ?? label;
  return (
    <FieldWrapper label={label} htmlFor={fieldId} error={error}>
      <select
        id={fieldId}
        className={`input-base ${error ? 'border-red-400' : ''} ${className}`}
        aria-invalid={Boolean(error)}
        {...rest}
      >
        {placeholder ? <option value="">{placeholder}</option> : null}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </FieldWrapper>
  );
}

-- Agregar campo id_tarea_padre para establecer relaciones padre-hijo entre tareas recurrentes
ALTER TABLE public.tarea 
ADD COLUMN id_tarea_padre BIGINT;

-- Agregar constraint de foreign key para referenciar la tabla tarea
ALTER TABLE public.tarea 
ADD CONSTRAINT fk_tarea_padre 
FOREIGN KEY (id_tarea_padre) REFERENCES public.tarea (id);

-- Crear índice para mejorar consultas por tarea padre
CREATE INDEX idx_tarea_padre ON public.tarea (id_tarea_padre);
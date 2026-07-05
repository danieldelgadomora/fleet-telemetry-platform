FROM python:3.11-slim
WORKDIR /app

COPY simulator/requirements.txt simulator/requirements.txt
RUN pip install --no-cache-dir -r simulator/requirements.txt

COPY simulator/ simulator/

ENTRYPOINT ["python", "-m", "simulator"]

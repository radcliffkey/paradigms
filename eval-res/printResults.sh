for f in *.log; do 
    echo -n "$f"$'\t'; 
    tail -n2 "$f" | head -n1 | grep -o '\[.*\]';
done

